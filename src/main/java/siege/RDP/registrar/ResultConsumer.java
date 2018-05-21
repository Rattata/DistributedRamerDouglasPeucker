package siege.RDP.registrar;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQSession;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IMessagingFactory;
import siege.RDP.messages.RDPResult;

public class ResultConsumer implements MessageListener {


	private IRDPRepository rdpRepo;
	private Logger log = Logger.getLogger(this.getClass());

	@Inject
	public ResultConsumer(RemoteConfig remotes, IRDPRepository repo) {
		this.rdpRepo = repo;
	}

	@Override
	public void onMessage(Message message) {

		try {
			Object objMessage = ((ObjectMessage) message).getObject();
			if (objMessage instanceof RDPResult) {
				RDPResult result = (RDPResult) objMessage;
				
				rdpRepo.update(result, message);
				
			} else {
				log.error("did not recognize object from queue");
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
	}
}
