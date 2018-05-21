package siege.RDP.data;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQSession;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

import siege.RDP.config.RemoteConfig;
import siege.RDP.messages.RDPAnnounce;
import siege.RDP.messages.RDPClean;
import siege.RDP.node.IStateMachine;

public class CleanupConsumer implements MessageListener {

	private Logger log = Logger.getLogger(this.getClass());
	private IRDPCache cache;
	private boolean listen_announce = false;
	
	@Inject
	public CleanupConsumer(RemoteConfig remote_config, IRDPCache cache){
		this.cache = cache;
	}

	public void setAnnounce(boolean announce) {
		this.listen_announce = announce;
	}
	
	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage objectMessage = ((ObjectMessage) message);
			Object object = objectMessage.getObject();
			if(object instanceof RDPClean){
				RDPClean clean = (RDPClean) objectMessage.getObject();
				log.info(String.format("%s clean", clean.Identifier()));
				cache.invalidate(clean.RDPId);
				message.acknowledge();
			} else if(object instanceof RDPAnnounce){
				RDPAnnounce announce = (RDPAnnounce) objectMessage.getObject();
				log.info(String.format("%s announce", announce.Identifier()));
				if(listen_announce){
					cache.getSegment(announce.RDPId, announce.start(), announce.end());
				}
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
	}
}
