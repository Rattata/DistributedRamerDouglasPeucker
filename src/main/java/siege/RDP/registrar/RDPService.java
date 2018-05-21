package siege.RDP.registrar;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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
import siege.RDP.messages.RDPAnnounce;
import siege.RDP.messages.RDPClean;
import siege.RDP.messages.RDPWork;
import siege.RDP.solver.ChunkingSearchFactory;
import siege.RDP.solver.ISearchStrategy;
import siege.RDP.solver.RDPIterateStrategy;

public class RDPService extends UnicastRemoteObject implements IRDPService {
	
	private IRDPRepository repo;
	private IIDGenerationService idGen;
	private TopicPublisher announce_producer;
	private MessageProducer work_producer;
	private ActiveMQSession jmssession;
	private Logger log = Logger.getLogger(this.getClass());
	private ChunkingSearchFactory searchFactory;
	
	@Inject
	public RDPService(IRDPRepository repository, ChunkingSearchFactory searchFactory, @Named("Segment") IIDGenerationService idGen,  IMessagingFactory messFact, RemoteConfig rconfig) throws RemoteException {
		this.repo = repository;
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
	
	@Override
	public <P extends IPoint> List<P> submit(List<P> points, double epsilon) throws RemoteException {
		return submit(points, epsilon, 0);
	}

	@Override
	public <P extends IPoint> List<P> submit(List<P> points, double epsilon, int partitions) throws RemoteException {
		log.info(String.format("received %d points for filtering  with %f epsilon ", points.size(), epsilon ));
		RDPContainer<P> container = repo.submit(points, epsilon);
		
		if(partitions > 0){
			RDPIterateStrategy solver = new RDPIterateStrategy();
			ISearchStrategy searcher = searchFactory.createSearcher( 200000, 8 );
			for(int i = 0; i < partitions; i++){
				solver.solve(new Line(container.GetLine()), epsilon, searcher);
			}
			
		}
		
		RDPWork work = new RDPWork(container.getId(), container.getInitialSegmentId(), -1,  0, points.size() - 1, 0);
		log.info(String.format("%s wrapped in work object", work.Identifier()));
		
		try {
			
			ObjectMessage workmessage = jmssession.createObjectMessage(work);
			workmessage.setStringProperty("JMSXGroupID", work.createNewPartitionString());
			
			work_producer.send(workmessage);
			log.info(String.format("%s sent to queue", work.Identifier()));
			
			RDPAnnounce announce = new RDPAnnounce(container.getId(), points.size());
			announce_producer.send(jmssession.createObjectMessage(announce));
			
			List<P> result = container.awaitResult();
			log.info(String.format("%s complete", work.Identifier()));

			RDPClean clean = new RDPClean(container.getId());
			log.info(String.format("%s cleanup", clean.Identifier()));
			announce_producer.send(jmssession.createObjectMessage(clean));
			repo.invalidate(container.getId());
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			return null;
		}		
	}
}
