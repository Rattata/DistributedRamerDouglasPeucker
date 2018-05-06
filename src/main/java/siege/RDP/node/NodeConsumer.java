package siege.RDP.node;

import java.io.Serializable;
import java.util.ArrayList;
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
import siege.RDP.data.StateMachine;
import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.Line;
import siege.RDP.messages.RDPExpect;
import siege.RDP.messages.RDPResult;
import siege.RDP.messages.RDPSearch;
import siege.RDP.messages.RDPSearchContainer;
import siege.RDP.messages.RDPWork;

public class NodeConsumer implements IStateMachine, Serializable {
	private transient Logger log = Logger.getLogger(NodeConsumer.class);
	private transient IRDPCache rdpCache;
	private transient StateMachine state = StateMachine.INIT;
	private transient NodeConfig nodeConfig;
	private transient MessageConsumer work_consumer;
	private transient MessageProducer result_producer;

	private transient MessageProducer work_producer;
	private transient IMessagingFactory messFact;
	private transient RemoteConfig remote_config = new RemoteConfig();
	private transient ExecutorService executor;

	@Inject
	public NodeConsumer(IRDPCache rdpCache, IMessagingFactory messagingFactory, ExecutorService executor,
			NodeConfigManager confmanager) {
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
					
					List<IOrderedPoint> segment = rdpCache.getSegment(work.RDPId, work.segmentStartIndex, work.endIndex);
					Line line = new Line(segment);
					
					log.info(String.format("%s started", identifier));
					double epsilon = rdpCache.getEpsilon(work.RDPId);
					RDPResult rdpRes = null;
					if(line.getPoints().size() < nodeConfig.split){
						log.info(String.format("%s local ", identifier));
						rdpRes = AllLocalRDP(line, epsilon, work.RDPId);
					} else {
						log.info(String.format("%s remote ", identifier));
						rdpRes = SplitRemoteRDP(line, epsilon, work.RDPId);
					}
					log.info(String.format("%s complete ", identifier));
					result_producer.send(messFact.createObjectMessage(rdpRes));
					
					msg.acknowledge();
					log.info(String.format("%s ack ", identifier));
					
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
	
	private RDPResult AllLocalRDP(Line line, double epsilon, int RDPID){
		Stack<Line> work = new Stack<>();
		work.push(line);
		
		ArrayList<Integer> results = new ArrayList<>();
		
		while(!work.isEmpty()){
			Line temp = work.pop();
			RDPSearchContainer searchContainer = new RDPSearchContainer(temp, nodeConfig.search_segments,
					executor);
			RDPSearch result = searchContainer.submitAndAwaitResult();
			if(result.furthestDistance > epsilon){
				for(Line split: temp.split(result.furthestIndex)){
					work.push(split);
				};
			} else {
				results.add(temp.getPoints().get(0).getIndex());
			}
		}
		
		int[] resultArray = new int[results.size()];
		IntStream.range(0, resultArray.length).map(x -> resultArray[x] = results.get(x));
		return new RDPResult(RDPID, line.getPoints().get(0).getIndex(), resultArray);
	}
	
	private RDPResult SplitRemoteRDP(Line line, double epsilon, int RDPID){
		Stack<Line> work = new Stack<>();
		work.push(line);
		
		ArrayList<Integer> results = new ArrayList<>();
		
		while(!work.isEmpty()){
			Line temp = work.pop();
			RDPSearchContainer searchContainer = new RDPSearchContainer(temp, nodeConfig.search_segments,
					executor);
			RDPSearch result = searchContainer.submitAndAwaitResult();
			if(result.furthestDistance > epsilon){
				List<Line> split = line.split(result.furthestIndex);
				work.push(split.get(0));
				CreateWork(split.get(1), RDPID);
			} else {
				results.add(temp.getPoints().get(0).getIndex());
			}
		}
		
		int[] resultArray = new int[results.size()];
		IntStream.range(0, resultArray.length).map(x -> resultArray[x] = results.get(x));
		return new RDPResult(RDPID, line.getPoints().get(0).getIndex(), resultArray);
	}
	
	private void CreateWork(Line line, int RDPId){
		RDPWork work = new RDPWork(
				RDPId, 
				line.getPoints().get(0).getIndex(), 
				line.getPoints().get(line.getPoints().size() - 1).getIndex());
		
		RDPExpect expect = new RDPExpect(RDPId, line.getPoints().get(0).getIndex());
		
		try {
			result_producer.send(messFact.createObjectMessage(expect));
			log.info(String.format("%s expect", expect.Identifier()));
			
			work_producer.send(messFact.createObjectMessage(work));
			log.info(String.format("%s create", work.Identifier()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
