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
import siege.RDP.data.RMIManager;
import siege.RDP.data.StateMachine;
import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.Line;
import siege.RDP.messages.RDPResult;
import siege.RDP.messages.RDPSearch;
import siege.RDP.messages.RDPSearchContainer;
import siege.RDP.messages.RDPWork;
import siege.RDP.registrar.ISegmentIDGenerator;

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
	private SegmentIDManager idMan;

	@Inject
	public NodeConsumer(IRDPCache rdpCache, SegmentIDManager idMan, RMIManager rmiMan,
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
					Line line = new Line(work.RDPId, work.segmentID, segment);

					log.info(String.format("%s started", identifier));
					double epsilon = rdpCache.getEpsilon(work.RDPId);
					msg.acknowledge();
					RDP(line, epsilon);
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

	private void RDP(Line line, double epsilon) {
		Stack<Line> work = new Stack<>();
		work.push(line);

		while (!work.isEmpty()) {
			Line temp = work.pop();
			RDPSearchContainer searchContainer = new RDPSearchContainer(temp, nodeConfig.search_segments, executor);
			RDPSearch result = searchContainer.submitAndAwaitResult();

			List<Integer> newSegments = new ArrayList<>();
			List<Integer> newResults = new ArrayList<>();
			RDPWork newWork = null;
			if (result.furthestDistance > epsilon) {
				List<Line> newLines = temp.split(result.furthestIndex, idMan.next(), idMan.next());
				if (temp.getPoints().size() > nodeConfig.split) {
					newWork = new RDPWork(temp.RDPID, temp.segmentID, line.start.getIndex(), line.end.getIndex());
					work.push(newLines.get(1));
				} else {
					work.push(newLines.get(0));
					work.push(newLines.get(1));
				}

			} else {
				newResults.add(temp.getPoints().get(0).getIndex());
			}
			
			RDPResult newUpdate = new RDPResult(temp.RDPID, temp.segmentID, newSegments, newResults);
			try {
				if(newWork != null){
					work_producer.send(messFact.createObjectMessage(newWork));					
				}
				result_producer.send(messFact.createObjectMessage(newUpdate));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	

}
