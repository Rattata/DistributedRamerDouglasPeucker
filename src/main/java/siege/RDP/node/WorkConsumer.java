package siege.RDP.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import siege.RDP.config.NodeConfig;
import siege.RDP.config.NodeConfigManager;
import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IMessagingFactory;
import siege.RDP.data.IRDPCache;
import siege.RDP.data.MessagingFactory;
import siege.RDP.data.RMIManager;
import siege.RDP.data.StateMachine;
import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.Line;
import siege.RDP.messages.RDPResult;
import siege.RDP.messages.RDPSearch;
import siege.RDP.messages.RDPSearchContainer;
import siege.RDP.messages.RDPWork;
import siege.RDP.registrar.IIDGenerationService;

public class WorkConsumer implements IStateMachine, Serializable {
	private transient Logger log = Logger.getLogger(WorkConsumer.class);
	private transient IRDPCache rdpCache;
	private transient StateMachine state = StateMachine.INIT;
	private transient NodeConfig nodeConfig;
	private transient MessageConsumer work_consumer;
	private transient MessageProducer result_producer;

	private transient MessageProducer work_producer;
	private transient IMessagingFactory messFact;
	private transient RemoteConfig remote_config = new RemoteConfig();
	private transient ExecutorService executor;
	private SegmentIDManager idMan;

	@Inject
	public WorkConsumer(IRDPCache rdpCache, SegmentIDManager idMan, RMIManager rmiMan,
			IMessagingFactory messagingFactory, ExecutorService executor, NodeConfigManager confmanager) {
		this.idMan = idMan;
		this.rdpCache = rdpCache;
		this.nodeConfig = confmanager.GetConfig();
		this.executor = executor;
		this.messFact = messagingFactory;

		this.work_consumer = messagingFactory.createMessageConsumer(remote_config.QUEUE_WORK);
		this.result_producer = messagingFactory.createMessageProducer(remote_config.QUEUE_RESULTS);
		this.work_producer = messagingFactory.createMessageProducer(remote_config.QUEUE_WORK);
		this.state = StateMachine.RUN;
		log.info("started");
	}

	@Override
	public void stop() {
		this.state = StateMachine.STOP;
	}

	@Override
	public Void call() throws Exception {
		while (state == StateMachine.RUN) {
			try {
				ObjectMessage msg = (ObjectMessage) work_consumer.receive(250);
				if (msg == null) {
					continue;
				}
				Object obj = msg.getObject();
				if (obj instanceof RDPWork) {
					RDPWork work = (RDPWork) obj;

					String identifier = work.Identifier();

					List<IOrderedPoint> segment = rdpCache.getSegment(work.RDPId, work.segmentStartIndex,
							work.endIndex);
					Line line = new Line(segment);

					log.info(String.format("%s started", identifier));
					double epsilon = rdpCache.getEpsilon(work.RDPId);
					RDPResult result = SplitRDP(line, epsilon, work.RDPId, work.segmentID, work.parentSegmentID);
					sendResult(result);
					
					msg.acknowledge();
					log.info(String.format("%s complete ", identifier));

				} else {
					log.error("could not deserialize object");
				}
			} catch (Exception e) {
				log.error(e);
				e.printStackTrace();
			}
		}
		log.info("stopped");
		return null;

	}

	private RDPResult SplitRDP(Line line, double epsilon, int RDPID, int SegmentID, int ParentSegmentID){

		List<Integer> newSegments = new ArrayList<>();
		List<Integer> segmentResultIndices = new ArrayList<>();
		RDPResult result = new RDPResult(RDPID, SegmentID, ParentSegmentID, newSegments, segmentResultIndices);
		
		Line temp = line;
		
		while (temp.getPoints().size() > nodeConfig.split) {
			
			RDPSearchContainer searchContainer = new RDPSearchContainer(temp, nodeConfig.search_segments, executor);
			RDPSearch search_result = searchContainer.submitAndAwaitResult();

			if (search_result.furthestDistance > epsilon) {
				
				List<Line> splitlines = temp.split(search_result.furthestIndex);
				boolean firstLarger = splitlines.get(0).getPoints().size() > splitlines.get(1).getPoints().size();
				Line larger = null;
				Line smaller = null;
				if(firstLarger){
					larger = splitlines.get(0);
					smaller = splitlines.get(1);
				} else {
					larger = splitlines.get(1);
					smaller = splitlines.get(0);
				}
				temp = larger;
				
				int newSegmentId = idMan.next();
				createWork(smaller, RDPID, SegmentID, newSegmentId);
				newSegments.add(newSegmentId);
				
			} else {
				segmentResultIndices.add(temp.start.getIndex());
				break;
			}
		}
		RDPResult completeResult = CompleteRDP(temp, epsilon, RDPID, SegmentID, ParentSegmentID) ;
		result.newSegments.addAll(completeResult.newSegments);
		result.segmentResultIndices.addAll(completeResult.segmentResultIndices);
		
		return result;
	}
	
	
	private RDPResult CompleteRDP(Line line, double epsilon, int RDPID, int SegmentID, int ParentSegmentID){
		List<Integer> newSegments = new ArrayList<>();
		List<Integer> segmentResultIndices = new ArrayList<>();
		RDPResult result = new RDPResult(RDPID, SegmentID, ParentSegmentID, newSegments, segmentResultIndices);

		Stack<Line> work = new Stack<>();
		work.push(line);
		while (!work.isEmpty()) {

			Line temp = work.pop();
			RDPSearchContainer searchContainer = new RDPSearchContainer(temp, nodeConfig.search_segments, executor);
			RDPSearch search_result = searchContainer.submitAndAwaitResult();

			if (search_result.furthestDistance > epsilon) {
				for (Line split : temp.split(search_result.furthestIndex)) {
					work.push(split);
				}
			} else {
				segmentResultIndices.add(temp.start.getIndex());
			}
		}
		return result;
	}

	private void createWork(Line line, int RDPID, int parentSegmentID, int SegmentID){
		RDPWork newWork = new RDPWork(RDPID, SegmentID, parentSegmentID, line.start.getIndex(), line.end.getIndex());
		sendWork(newWork);
	}
	
	private void sendResult(RDPResult result) {
		try {
			result_producer.send(messFact.createObjectMessage(result));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendWork(RDPWork work) {
		try {
			work_producer.send(messFact.createObjectMessage(work));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

}
