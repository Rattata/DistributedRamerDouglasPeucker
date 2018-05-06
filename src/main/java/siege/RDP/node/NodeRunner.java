package siege.RDP.node;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.google.inject.Injector;

import siege.RDP.Node;
import siege.RDP.config.NodeConfig;
import siege.RDP.config.NodeConfigManager;
import siege.RDP.data.CleanupConsumer;

public class NodeRunner extends UnicastRemoteObject implements IUpdatableNode {
	private static final String REGISTRATION_NAME = "NodeRunner";
	private transient Logger log = Logger.getLogger(this.getClass());

	private transient NodeConfigManager config_man;
	private transient NodeConfig config;

	private transient ExecutorService executor;

	private transient ArrayList<IStateMachine> stoppable = new ArrayList<>();
	private transient List<Future<Void>> completions;

	@Inject
	public NodeRunner(NodeConfigManager configman, ExecutorService executor) throws RemoteException {
		this.config_man = configman;
		config = config_man.GetConfig();
		this.executor = executor;
	}

	@Override
	public void update(NodeConfig update) throws RemoteException {
		stop();
		this.config = update;
		config_man.WriteConfig(update);
		start();
	}

	public static IUpdatableNode connect(String host, int port) {
		IUpdatableNode registrationService = null;
		try {
			Registry r = LocateRegistry.getRegistry(host, port);
			Remote obj = r.lookup(REGISTRATION_NAME);
			registrationService = (IUpdatableNode) obj;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return registrationService;
	}

	public void start() {
		log.info("start");
		Injector inject = Node.injector;
		stoppable = new ArrayList<>();
		for (int i = 0; i < config.consumers; i++) {
			NodeConsumer consumer = inject.getInstance(NodeConsumer.class);
			stoppable.add(consumer);
		}
		CleanupConsumer cleanup = inject.getInstance(CleanupConsumer.class);
		stoppable.add(cleanup);

		try {
			completions = executor.invokeAll(stoppable);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("started");
	}

	private void stop() {
		log.info("stop");
		for (IStateMachine machines : stoppable) {
			machines.stop();
		}
		try {
			for (Future<Void> machines : completions) {
				machines.get();
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
	}

}
