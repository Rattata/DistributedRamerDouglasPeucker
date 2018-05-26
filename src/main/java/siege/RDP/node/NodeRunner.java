package siege.RDP.node;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQSession;
import org.apache.log4j.Logger;

import com.google.inject.Injector;

import siege.RDP.Node;
import siege.RDP.config.NodeConfig;
import siege.RDP.config.NodeConfigManager;
import siege.RDP.config.RemoteConfig;
import siege.RDP.data.CleanupConsumer;
import siege.RDP.data.MessagingFactory;

public class NodeRunner extends UnicastRemoteObject implements IUpdatableNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6625654181215125876L;

	private transient Logger log = Logger.getLogger(this.getClass());

	private transient NodeConfigManager config_man;
	private transient NodeConfig config;
	private RemoteConfig remote_conf = new RemoteConfig();
	private MessagingFactory messFact;
	private transient ArrayList<ActiveMQSession> sessions = new ArrayList<>();

	@Inject
	public NodeRunner(NodeConfigManager configman, CleanupConsumer cleanupJob, MessagingFactory messFact)
			throws RemoteException {
		this.config_man = configman;
		config = config_man.GetConfig();
		this.messFact = messFact;
		try {
			ActiveMQSession session = messFact.getSession();
			Topic cleanuptopic = session.createTopic(remote_conf.TOPIC_CLEANUP);
			session.createSubscriber(cleanuptopic).setMessageListener(cleanupJob);
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
	}

	@Override
	public void update(NodeConfig update) throws RemoteException {
		stop();
		log.info(update.toString());
		this.config = update;
		config_man.WriteConfig(update);
		start();
	}

	public void start() {
		log.info("start");
		Injector inject = Node.injector;
		sessions = new ArrayList<>();
		try {
			for (int i = 0; i < config.consumers; i++) {
				WorkConsumer consumer = inject.getInstance(WorkConsumer.class);
				ActiveMQSession session = messFact.getSession();
				Queue work_queue = session.createQueue(remote_conf.QUEUE_WORK);
				session.createConsumer(work_queue, consumer);
				sessions.add(session);
			}
		} catch (JMSException e) {
			log.fatal(e);
			e.printStackTrace();
		}
		log.info("started");
	}

	private void stop() {
		log.info("stop");
		try {
			for (ActiveMQSession session : sessions) {
				session.close();
			}
			sessions = new ArrayList<>();
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
		log.info("stopped");
	}

}
