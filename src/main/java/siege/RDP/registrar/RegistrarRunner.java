package siege.RDP.registrar;

import javax.inject.Singleton;
import javax.jms.Destination;

import org.apache.activemq.ActiveMQSession;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.MessagingFactory;
import siege.RDP.data.RMIManager;
@Singleton
public class RegistrarRunner {

	ResultConsumer consumer;
	IRDPService  service;
	RMIManager rmiMan;
	IRDPRepository repo;
	RemoteConfig remote_config;
	IIDGenerationService idGen;
	MessagingFactory messFact;
	
	Logger log = Logger.getLogger(this.getClass());
	
	@Inject
	public RegistrarRunner(MessagingFactory messFact, ResultConsumer consumer, @Named("Segment") IIDGenerationService idGen, RemoteConfig remote_config, IRDPService service, RMIManager rmiMan, IRDPRepository repo) {
		this.consumer = consumer;
		this.service = service;
		this.rmiMan = rmiMan;
		this.repo = repo;
		this.remote_config = remote_config;
		this.idGen = idGen;
		this.messFact = messFact;
		
		try {
			ActiveMQSession session = messFact.getSession();
			Destination results = session.createQueue(remote_config.QUEUE_RESULTS);
			session.createConsumer(results, consumer);
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
		System.setProperty("java.rmi.server.hostname",remote_config.REGISTRATION_MASTER);
		rmiMan.RegisterRdpService( service);
		rmiMan.RegisterRepository(repo);
		rmiMan.RegisterIDGenerator(idGen);
	}

}