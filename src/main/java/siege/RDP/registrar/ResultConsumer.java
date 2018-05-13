package siege.RDP.registrar;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IMessagingFactory;
import siege.RDP.messages.RDPResult;

public class ResultConsumer {

	private MessageConsumer expectConsumer;

	private IRDPRepository rdpRepo;

	private Logger log = Logger.getLogger(this.getClass());

	@Inject
	public ResultConsumer(IMessagingFactory fact, RemoteConfig remotes, IRDPRepository repo) {
		expectConsumer = fact.createMessageConsumer(remotes.QUEUE_RESULTS);
		this.rdpRepo = repo;
	}

	private LinkedList<ObjectMessage> resultBuffer = new LinkedList<>();

	public void execute() {
		ObjectMessage msg = null;
		Message rcv = null;
		try {
			rcv = expectConsumer.receive(5);
			if (rcv != null) {
				msg = (ObjectMessage) rcv;
			} else {
				if (!resultBuffer.isEmpty()) {
					msg = resultBuffer.removeFirst();
				}
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
		if (msg != null) {
			try {
				Object objMessage = msg.getObject();
				if (objMessage instanceof RDPResult) {
					RDPResult result = (RDPResult) objMessage;
					log.info(String.format("%s rcv result", result.Identifier()));
					boolean wasExpected = rdpRepo.update(result);
					log.info(String.format("%s rcv result %b", result.Identifier(), wasExpected));
					rcv.acknowledge();
				} else {
					log.error("did not recognize object from queue");
				}
			} catch (Exception e) {
				log.error(e);
				e.printStackTrace();
			}
		}
	}
}
