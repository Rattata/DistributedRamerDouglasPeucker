package siege.RDP;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IMessagingFactory;
import siege.RDP.data.MessagingFactory;
import siege.RDP.data.RMIManager;
import siege.RDP.registrar.IRDPRepository;
import siege.RDP.registrar.IRDPService;
import siege.RDP.registrar.IIDGenerationService;
import siege.RDP.registrar.IdentityGenerator;
import siege.RDP.registrar.RDPRepository;
import siege.RDP.registrar.RDPService;
import siege.RDP.registrar.RegistrarRunner;
import siege.RDP.registrar.ResultConsumer;

public class RegistrarContainer extends AbstractModule {
	IRDPMode rdp;
	public RegistrarContainer(IRDPMode rdp) {
		this.rdp = rdp;
		try {
			segmentGenerator = new IdentityGenerator();
			RDPGenerator = new IdentityGenerator();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	IdentityGenerator segmentGenerator;
	IdentityGenerator RDPGenerator;
	
	@Override
	protected void configure() {
		bind(IRDPRepository.class).to(RDPRepository.class);
		bind(IRDPService.class).to(RDPService.class);
		bind(IIDGenerationService.class).annotatedWith(Names.named("Segment")).toInstance(segmentGenerator);
		bind(IIDGenerationService.class).annotatedWith(Names.named("RDP")).toInstance(RDPGenerator);

		bind(RegistrarRunner.class);
		bind(ResultConsumer.class);
		bind(RMIManager.class);
		bind(ExecutorService.class).toInstance(Executors.newWorkStealingPool(12));
		bind(IMessagingFactory.class).to(MessagingFactory.class);
		bind(RemoteConfig.class).toInstance(RemoteConfig.ReadConfig());
		bind(IRDPMode.class).toInstance(rdp);
	}
	
}
