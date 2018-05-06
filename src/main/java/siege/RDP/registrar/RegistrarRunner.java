package siege.RDP.registrar;

import javax.inject.Singleton;

import com.google.inject.Inject;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.RMIManager;
@Singleton
public class RegistrarRunner implements Runnable {

	ResultConsumer consumer;
	IRDPService  service;
	RMIManager rmiMan;
	IRDPRepository repo;
	RemoteConfig remote_config;
	
	@Inject
	public RegistrarRunner(ResultConsumer consumer, RemoteConfig remote_config, IRDPService service, RMIManager rmiMan, IRDPRepository repo) {
		this.consumer = consumer;
		this.service = service;
		this.rmiMan = rmiMan;
		this.repo = repo;
		this.remote_config = remote_config;
	}
	
	@Override
	public void run() {
		System.setProperty("java.rmi.server.hostname",remote_config.REGISTRATION_MASTER);
		rmiMan.RegisterRdpService( service);
		rmiMan.RegisterRepository(repo);
		
		while(true){
			consumer.execute();
		}

	}
	
}