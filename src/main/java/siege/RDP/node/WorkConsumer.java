package siege.RDP.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import org.apache.activemq.ActiveMQSession;
import org.apache.log4j.Logger;

import siege.RDP.config.NodeConfig;
import siege.RDP.config.NodeConfigManager;
import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IRDPCache;
import siege.RDP.data.MessagingFactory;
import siege.RDP.data.RMIManager;
import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.Line;
import siege.RDP.domain.RDPIteration;
import siege.RDP.messages.RDPResult;
import siege.RDP.messages.RDPWork;
import siege.RDP.solver.ChunkingSearchFactory;
import siege.RDP.solver.IRDPstrategy;
import siege.RDP.solver.ISearchStrategy;
import siege.RDP.solver.RDPCompleteStrategy;

public class WorkConsumer implements Serializable, MessageListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3486695205029235167L;
	private Logger log = Logger.getLogger(WorkConsumer.class);
	private IRDPCache rdpCache;
	private NodeConfig nodeConfig;

	private ActiveMQSession jmssession;
	private MessageProducer result_producer;
	
	private RemoteConfig remote_config = new RemoteConfig();
	
	private ISearchStrategy searchStrategy;
	private IRDPstrategy rdpStrategy;
	

	@Inject
	public WorkConsumer(IRDPCache rdpCache, RMIManager rmiMan, ExecutorService executor,
			MessagingFactory messFact, NodeConfigManager confmanager) {
		this.rdpCache = rdpCache;
		this.nodeConfig = confmanager.GetConfig();
		
		try {
			this.jmssession = messFact.getSession();
			Queue results = jmssession.createQueue(remote_config.QUEUE_RESULTS);

			this.result_producer = jmssession.createProducer(results);
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
		ChunkingSearchFactory searchfactory = new ChunkingSearchFactory(executor);
		searchStrategy = searchfactory.createSearcher(nodeConfig.search_chunk_size, nodeConfig.max_partitions);
		
		rdpStrategy = new RDPCompleteStrategy();
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
				log.info(String.format("consuming work: %s", work.toString()));
				List<IOrderedPoint> segmentPoints =  rdpCache.getSegment(work.RDPId, work.segmentStartIndex, work.endIndex);
				
				Line segment = new Line(segmentPoints);
				
				double epsilon = rdpCache.getEpsilon(work.RDPId);
				
				RDPIteration iteration = rdpStrategy.solve(segment, epsilon, searchStrategy);
				
				SendUpdate(work.RDPId, work.segmentID, work.parentSegmentID, iteration);
				
				msg.acknowledge();
				log.info(String.format("consuming work DONE: %s", work.toString()));
				
			} else {
				log.error("could not deserialize object");
			}
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
	}


	private void SendUpdate(int RDPId, int segmentId, int parentSegmentId,  RDPIteration iteration){
			try {

			RDPResult result = new RDPResult(RDPId, segmentId, parentSegmentId, new ArrayList<Integer>(), iteration.getResultPoints());
			
			ObjectMessage msg = jmssession.createObjectMessage(result);
			result_producer.send(msg);

			log.info(String.format("RDP: sent Result %s", result.Identifier()));

		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
	}
	
}
