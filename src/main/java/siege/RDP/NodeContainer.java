package siege.RDP;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.AbstractModule;

import siege.RDP.config.NodeConfigManager;
import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IMessagingFactory;
import siege.RDP.data.IRDPCache;
import siege.RDP.data.MessagingFactory;
import siege.RDP.data.RDPCache;
import siege.RDP.data.RMIManager;
import siege.RDP.node.WorkConsumer;
import siege.RDP.node.NodeRunner;

public class NodeContainer extends AbstractModule {
	private IRDPMode mode;
	public NodeContainer(IRDPMode rdp ) {
		this.mode = rdp;
	}
	
	@Override
	protected void configure() {
		bind(NodeRunner.class);
		bind(RemoteConfig.class).toInstance(RemoteConfig.ReadConfig());
		bind(IRDPCache.class).to(RDPCache.class).asEagerSingleton();
		bind(IMessagingFactory.class).to(MessagingFactory.class).asEagerSingleton();
		bind(WorkConsumer.class);
		bind(NodeConfigManager.class);
		bind(RMIManager.class).asEagerSingleton();
		bind(ExecutorService.class).toInstance(Executors.newWorkStealingPool(8));
		bind(IRDPMode.class).toInstance(mode);
	}
		
	
	
}
