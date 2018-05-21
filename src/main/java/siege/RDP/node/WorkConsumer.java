package siege.RDP.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.xml.soap.MessageFactory;

import org.apache.activemq.ActiveMQSession;
import org.apache.log4j.Logger;

import siege.RDP.config.NodeConfig;
import siege.RDP.config.NodeConfigManager;
import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IMessagingFactory;
import siege.RDP.data.IRDPCache;
import siege.RDP.data.MessagingFactory;
import siege.RDP.data.RMIManager;
import siege.RDP.data.WorkSkeleton;
import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.Line;
import siege.RDP.messages.RDPResult;
import siege.RDP.messages.RDPWork;
import siege.RDP.solver.SearchJob;
import siege.RDP.solver.ChunkingSearchFactory;

public class WorkConsumer implements Serializable, MessageListener {
	private Logger log = Logger.getLogger(WorkConsumer.class);
	private IRDPCache rdpCache;
	private NodeConfig nodeConfig;

	private ActiveMQSession jmssession;
	private MessageProducer result_producer;
	private MessageProducer work_producer;

	private RemoteConfig remote_config = new RemoteConfig();
	private ExecutorService executor;
	private SegmentIDManager idMan;

	@Inject
	public WorkConsumer(IRDPCache rdpCache, SegmentIDManager idMan, RMIManager rmiMan, ExecutorService executor,
			MessagingFactory messFact, NodeConfigManager confmanager) {
		this.idMan = idMan;
		this.rdpCache = rdpCache;
		this.nodeConfig = confmanager.GetConfig();
		this.executor = executor;
		try {
			this.jmssession = messFact.getSession();
			Queue work = jmssession.createQueue(remote_config.QUEUE_WORK);
			Queue results = jmssession.createQueue(remote_config.QUEUE_RESULTS);

			this.result_producer = jmssession.createProducer(results);
			this.work_producer = jmssession.createProducer(work);
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
	}

	public ActiveMQSession GetSession() {
		return jmssession;
	}

	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage msg = (ObjectMessage) message;

			Object obj = msg.getObject();
			if (obj instanceof RDPWork) {
				RDPWork work = (RDPWork) obj;

				String identifier = work.Identifier();

				CompletableFuture<WorkSkeleton> skeletonFuture1 = CompletableFuture
						.supplyAsync(() -> new WorkSkeleton(message, work), executor);
				
				CompletableFuture<WorkSkeleton> skeletonFuture2 = skeletonFuture1.thenApplyAsync((skeleton) -> skeleton.setLine(rdpCache.getSegment(skeleton.work.RDPId,
						skeleton.work.segmentStartIndex, skeleton.work.endIndex)), executor);

				CompletableFuture<WorkSkeleton> skeletonFuture3 = skeletonFuture2.thenApplyAsync(
						(skeleton) -> skeleton.setEpsilon(rdpCache.getEpsilon(skeleton.work.RDPId)), executor);

				
				skeletonFuture3.thenAccept((skeleton) -> SplitRDP(skeleton));
				
				CompletableFuture.supplyAsync(() -> skeletonFuture3.join(), executor );

			} else {
				log.error("could not deserialize object");
			}
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
	}

	public void exceptionally(Throwable exception) {
		log.fatal(exception);
		exception.printStackTrace();
	}

	private WorkSkeleton SplitRDP(WorkSkeleton skeleton) {
		log.info(String.format("RDP: phase: split %s", skeleton.work.Identifier()));
		Line line = skeleton.line;
		double epsilon = skeleton.epsilon;
		
		RDPWork originalWork = skeleton.work;
		int originalPartition = originalWork.partition_ancestors;
		String oldPartitionString = skeleton.PartitionID;

		int RDPID = originalWork.RDPId;
		int SegmentID = originalWork.segmentID;
		int ParentSegmentID = originalWork.parentSegmentID;

		try {
			List<Integer> newSegments = new ArrayList<>();
			List<Integer> segmentResultIndices = new ArrayList<>();
			RDPResult result = new RDPResult(RDPID, SegmentID, ParentSegmentID, newSegments, segmentResultIndices);
			Line temp = line;

			while (originalPartition++ <= nodeConfig.max_partitions) {
				
				ChunkingSearchFactory searchContainer = new ChunkingSearchFactory(temp, nodeConfig.search_chunk_size, nodeConfig.cores, executor);
				
				SearchJob search_result = searchContainer.submitAndAwaitResult();

				if (search_result.furthestDistance > epsilon) {

					List<Line> splitlines = temp.split(search_result.furthestIndex);
					boolean firstLarger = splitlines.get(0).getPoints().size() > splitlines.get(1).getPoints().size();
					Line larger = null;
					Line smaller = null;
					if (firstLarger) {
						larger = splitlines.get(0);
						smaller = splitlines.get(1);
					} else {
						larger = splitlines.get(1);
						smaller = splitlines.get(0);
					}
					temp = larger;

					int newSegmentId = idMan.next();

					createWork(smaller, RDPID, SegmentID, newSegmentId, originalWork.partition_ancestors, oldPartitionString);
					newSegments.add(newSegmentId);

				} else {
					segmentResultIndices.add(temp.start.getIndex());
					break;
				}
			}
			RDPResult completeResult = CompleteRDP(temp, epsilon, originalWork);
			result.newSegments.addAll(completeResult.newSegments);
			result.segmentResultIndices.addAll(completeResult.segmentResultIndices);

			sendResult(result);

			skeleton.originalMessage.acknowledge();

		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
		log.info(String.format("RDP: phase: done %s", skeleton.work.Identifier()));
		return skeleton;
	}

	private void createWork(Line line, int RDPID, int newParentSegmentID, int newSegmentID, int partition_ancestors,
			String parentPartitionString) {
		try {
			log.info(String.format("create work: %d:%d:&d", RDPID, newParentSegmentID, newSegmentID));
			int new_partition_ancestors = partition_ancestors+1;
			
			RDPWork work = new RDPWork(RDPID, newSegmentID, newParentSegmentID, line.start.getIndex(),
					line.end.getIndex(), new_partition_ancestors);

			String partitionString = work.createNewPartitionString();

			ObjectMessage msg = jmssession.createObjectMessage(work);
			msg.setStringProperty("JMSXGroupID", partitionString);
			
			work_producer.send(msg);
			log.info(String.format("created work: %d:%d:&d -> %s", RDPID, newParentSegmentID, newSegmentID, partitionString));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private RDPResult CompleteRDP(Line line, double epsilon, RDPWork originalWork) {

		log.info(String.format("RDP: phase: local %s", originalWork.Identifier()));
		int RDPID = originalWork.RDPId;
		int SegmentID = originalWork.segmentID;
		int ParentSegmentID = originalWork.parentSegmentID;

		List<Integer> newSegments = new ArrayList<>();
		List<Integer> segmentResultIndices = new ArrayList<>();
		RDPResult result = new RDPResult(RDPID, SegmentID, ParentSegmentID, newSegments, segmentResultIndices);

		Stack<Line> work = new Stack<>();
		work.push(line);
		while (!work.isEmpty()) {

			Line temp = work.pop();
			ChunkingSearchFactory searchContainer = new ChunkingSearchFactory(temp, nodeConfig.search_chunk_size,nodeConfig.cores, executor);
			SearchJob search_result = searchContainer.submitAndAwaitResult();

			if (search_result.furthestDistance > epsilon) {
				for (Line split : temp.split(search_result.furthestIndex)) {
					work.push(split);
				}
			} else {
				segmentResultIndices.add(temp.start.getIndex());
			}
		}

		log.info(String.format("RDP: phase: local done %s", originalWork.Identifier()));

		return result;
	}

	private void sendResult(RDPResult result) {
		try {

			log.info(String.format("RDP: sendResult %s", result.Identifier()));

			ObjectMessage msg = jmssession.createObjectMessage(result);
			result_producer.send(msg);

			log.info(String.format("RDP: sentResult %s", result.Identifier()));

		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
	}

}
