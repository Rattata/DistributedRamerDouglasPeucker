package siege.RDP.registrar;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import javax.jms.MessageProducer;

import org.testng.log4testng.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IMessagingFactory;
import siege.RDP.domain.IPoint;
import siege.RDP.messages.RDPClean;
import siege.RDP.messages.RDPWork;

public class RDPService extends UnicastRemoteObject implements IRDPService {
	
	private IRDPRepository repo;
	private IMessagingFactory messFact;
	private IIDGenerationService idGen;
	private MessageProducer clean_producer;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Inject
	public RDPService(IRDPRepository repository, @Named("Segment") IIDGenerationService idGen,  IMessagingFactory messFact, RemoteConfig rconfig) throws RemoteException {
		this.repo = repository;
		this.messFact = messFact;
		this.idGen = idGen;
		this.clean_producer = messFact.createTopicProducer(rconfig.TOPIC_CLEANUP);
	}
	
	@Override
	public <P extends IPoint> List<P> submit(List<P> points, double epsilon) throws RemoteException {
		log.info(String.format("received %d points for filtering  with %f epsilon ", points.size(), epsilon ));
		RDPContainer<P> container = repo.submit(points, epsilon);
		RDPWork work = new RDPWork(container.getId(), idGen.next() , -1,  0, points.size() - 1);
		
		try {
			
			List<P> result = container.awaitResult();
			log.info(String.format("%s complete", work.Identifier()));

			RDPClean clean = new RDPClean(container.getId());
			log.info(String.format("%s cleanup", clean.Identifier()));
			clean_producer.send(messFact.createObjectMessage(clean));
			repo.removeContainer(container.getId());
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			return null;
		}		
	}
}
