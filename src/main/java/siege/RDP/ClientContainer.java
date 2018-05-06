package siege.RDP;

import com.google.inject.AbstractModule;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.RMIManager;

public class ClientContainer extends AbstractModule {
	
	IRDPMode mode;
	public ClientContainer(IRDPMode mode) {
		this.mode= mode;
	}
	
	@Override
	protected void configure() {
		bind(IRDPMode.class).toInstance(mode);
		bind(RMIManager.class);
		bind(RemoteConfig.class).toInstance(RemoteConfig.ReadConfig());
	}
}
