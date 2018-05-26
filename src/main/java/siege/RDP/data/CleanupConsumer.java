package siege.RDP.data;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

import siege.RDP.config.RemoteConfig;
import siege.RDP.messages.RDPAnnounce;
import siege.RDP.messages.RDPClean;

public class CleanupConsumer implements MessageListener {

	private Logger log = Logger.getLogger(this.getClass());
	private IRDPCache cache;
	
	@Inject
	public CleanupConsumer(RemoteConfig remote_config, IRDPCache cache){
		this.cache = cache;
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
				cache.getEpsilon(announce.RDPId);
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
	}
}
