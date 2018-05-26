package siege.RDP;

import com.google.inject.Guice;
import com.google.inject.Injector;

import siege.RDP.data.RMIManager;
import siege.RDP.node.NodeRunner;

public class Node implements IRDPMode {
	public static Injector injector;
	
	public static void main(String[] args) {
		Node.injector = Guice.createInjector(new NodeContainer(new Node()));
		NodeRunner runner = Node.injector.getInstance(NodeRunner.class);
		RMIManager remoteMan = Node.injector.getInstance(RMIManager.class);
		
		
		remoteMan.RegisterUpdatableNode(runner);
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "20");
		runner.start();
	}

	@Override
	public RDPMode rdpMode() {
		return RDPMode.NODE;
	}

}
