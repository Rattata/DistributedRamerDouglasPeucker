package siege.RDP;

import com.google.inject.AbstractModule;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IMessagingFactory;
import siege.RDP.data.MessagingFactory;
import siege.RDP.data.RMIManager;
import siege.RDP.registrar.IRDPRepository;
import siege.RDP.registrar.IRDPService;
import siege.RDP.registrar.ISegmentIDGenerator;
import siege.RDP.registrar.IdentityGenerator;
import siege.RDP.registrar.RDPRepository;
import siege.RDP.registrar.RDPService;
import siege.RDP.registrar.RegistrarRunner;
import siege.RDP.registrar.ResultConsumer;

public class RegistrarContainer extends AbstractModule {
	IRDPMode rdp;
	public RegistrarContainer(IRDPMode rdp) {
		this.rdp = rdp;
	}
	
	@Override
	protected void configure() {
		bind(IRDPRepository.class).to(RDPRepository.class);
		bind(IRDPService.class).to(RDPService.class);
		bind(ISegmentIDGenerator.class).to(IdentityGenerator.class);
		bind(RegistrarRunner.class);
		bind(ResultConsumer.class);
		bind(RMIManager.class);
		bind(IMessagingFactory.class).to(MessagingFactory.class);
		bind(RemoteConfig.class).toInstance(RemoteConfig.ReadConfig());
		bind(IRDPMode.class).toInstance(rdp);
	}
	
}
