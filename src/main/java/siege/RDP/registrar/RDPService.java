package siege.RDP.registrar;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

import org.apache.activemq.ActiveMQSession;
import org.testng.log4testng.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IMessagingFactory;
import siege.RDP.domain.IPoint;
import siege.RDP.domain.Line;
import siege.RDP.domain.RDPIteration;
import siege.RDP.messages.RDPClean;
import siege.RDP.messages.RDPWork;
import siege.RDP.solver.ChunkingSearchFactory;
import siege.RDP.solver.ISearchStrategy;
import siege.RDP.solver.RDPIterateStrategy;

public class RDPService extends UnicastRemoteObject implements IRDPService {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6506962405976544706L;
	private IRDPRepository calculationRepository;
	private IIDGenerationService idGen;
	private TopicPublisher announce_producer;
	private MessageProducer work_producer;
	private ActiveMQSession jmssession;
	private Logger log = Logger.getLogger(this.getClass());
	private ChunkingSearchFactory searchFactory;
	
	@Inject
	public RDPService(IRDPRepository repository, ChunkingSearchFactory searchFactory, @Named("Segment") IIDGenerationService idGen,  IMessagingFactory messFact, RemoteConfig rconfig) throws RemoteException {
		this.calculationRepository = repository;
		this.idGen = idGen;
		this.searchFactory = searchFactory;
		try {
			this.jmssession = messFact.getSession();
			Topic t = jmssession.createTopic(rconfig.TOPIC_CLEANUP);
			this.announce_producer = jmssession.createPublisher(t);
			Queue q = jmssession.createQueue(rconfig.QUEUE_WORK);
			this.work_producer = jmssession.createProducer(q);
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
	}
	
	public <P extends IPoint>  void CreateWork(ICalculationContainer<P> container, int partition_depth, double epsilon) throws RemoteException{
		
		if(partition_depth > 0){
			
			List<Line> partitionedLines = new ArrayList<>();
			Line inputLine = new Line(container.getLine());
			partitionedLines.add(inputLine);
			
			List<Integer> partitionedResults = new ArrayList<>();

			RDPIterateStrategy solver = new RDPIterateStrategy();
			ISearchStrategy searcher = searchFactory.createSearcher( 20000, 8 );
			
			for(int i = 0; i < partition_depth; i++){
				List<Line> templines = new ArrayList<>();
				for(Line partition : partitionedLines ){
					RDPIteration iterationass = solver.solve(partition, epsilon, searcher);
					templines.addAll(iterationass.getNewLines());
					partitionedResults.addAll(iterationass.getResultPoints());
				}
				partitionedLines = templines;
			}
			
			container.putResults(partitionedResults);
			log.info(String.format("%d Already solved %d points", container.getRDPId(), partitionedResults.size()));
			
			for(Line line : partitionedLines){
				int segmentID = calculationRepository.ExpectSegment(container.getRDPId(), line.getPoints());
				sendWork(container.getRDPId(), segmentID, line.start.getIndex(), line.end.getIndex());
			}
			
		} else {
			Line completeline = new Line(container.getLine());
			int segmentID = calculationRepository.ExpectSegment(container.getRDPId(), completeline.getPoints());
			sendWork(container.getRDPId(), segmentID, completeline.start.getIndex(), completeline.end.getIndex());
		}
	}

	private void sendWork(int RDPID, int segmentId, int start, int end){
		log.info(String.format("%d wrapped in work object", RDPID));
		
		RDPWork work = new RDPWork(RDPID, segmentId, -1,  start, end, 0);
		try {
			ObjectMessage oMessage = jmssession.createObjectMessage(work);
			String partition = work.createNewPartitionString();
			oMessage.setStringProperty("JMSXGroupID", partition);
			
			work_producer.send(oMessage);		
			log.info(String.format("sent %d wrapped %s", RDPID,  partition));
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public <P extends IPoint> List<P> submit(List<P> points, double epsilon) throws RemoteException {
		return submit(points, epsilon, 0);
	}
	
	@Override
	public <P extends IPoint> List<P> submit(List<P> points, double epsilon, int partitions) throws RemoteException {
		log.info(String.format("received %d points for filtering  with %f epsilon ", points.size(), epsilon ));
		
		ICalculationContainer<P> container = calculationRepository.submitCalculation(points, epsilon);
		
		try {
			
			CreateWork(container, partitions, epsilon);
			
			List<P> result = container.awaitResult();
			log.info(String.format("%d complete", container.getRDPId()));
			
			RDPClean clean = new RDPClean(container.getRDPId());
			log.info(String.format("%s cleanup", clean.Identifier()));
			announce_producer.send(jmssession.createObjectMessage(clean));
			
			calculationRepository.invalidate(container.getRDPId());
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			return null;
		}		
	}
}
