package siege.RDP.registrar;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

import siege.RDP.config.RemoteConfig;
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
