package siege.RDP.data;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import siege.RDP.config.RemoteConfig;
import siege.RDP.messages.RDPClean;
import siege.RDP.node.IStateMachine;

public class CleanupConsumer implements IStateMachine {

	private MessageConsumer consumer;
	
	private StateMachine state = StateMachine.INIT;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private IRDPCache cache;
	
	@Inject
	public CleanupConsumer(IMessagingFactory messagingFactory, RemoteConfig remote_config, IRDPCache cache){
		consumer = messagingFactory.createTopicConsumer(remote_config.TOPIC_CLEANUP);
		this.cache = cache;
		this.state = StateMachine.RUN;
		log.info("started");
	}

	@Override
	public Void call() throws Exception {
		while(state == StateMachine.RUN){
			try {
				Message message = consumer.receive(150);
				if(message == null){
					continue;
				}
				RDPClean clean = (RDPClean) ((ObjectMessage) message).getObject();
				log.info(String.format("%s", clean.Identifier()));
				cache.invalidate(clean.RDPId);
				message.acknowledge();
			} catch (Exception e) {
				log.error(e);
				e.printStackTrace();
			}
		}
		log.info("stopped");
		return null;
	}

	@Override
	public void stop() {
		state = StateMachine.STOP;
	}
}
