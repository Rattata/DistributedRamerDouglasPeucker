package siege.RDP.registrar;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.IMessagingFactory;
import siege.RDP.messages.RDPExpect;
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

	public void execute() {
		ObjectMessage msg = null;
		Message rcv = null;
		try {
			rcv = expectConsumer.receive(125);
			if (rcv != null) {
				msg = (ObjectMessage) rcv;
			} else {
				return;
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
		if (msg != null) {
			try {
				Object objMessage = msg.getObject();
				if (objMessage instanceof RDPExpect) {
					RDPExpect expectMsg = (RDPExpect) msg.getObject();
					log.info(String.format("%s rcv expect", expectMsg.Identifier()));
					rdpRepo.signalExpectation(expectMsg.RDPID, expectMsg.ResultStartIndex);
					rcv.acknowledge();
				} else if (objMessage instanceof RDPResult) {
					RDPResult result = (RDPResult) objMessage;
					log.info(String.format("%s rcv result", result.Identifier()));
					boolean wasExpected = rdpRepo.finalizeExpectation(result.RDPId, result.segmentStartIndex, result.segmentResultIndices);
					if(wasExpected){
						rcv.acknowledge();
						log.info(String.format("%s rcv result expected", result.Identifier()));
					} else {
						log.info(String.format("%s rcv result unexpected", result.Identifier()));
					}
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
