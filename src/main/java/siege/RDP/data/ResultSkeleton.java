package siege.RDP.data;

import javax.jms.Message;

import siege.RDP.messages.RDPResult;
import siege.RDP.registrar.RDPContainer;

public class ResultSkeleton {
	public RDPResult originalResult;
	public RDPContainer<?> container;
	public Message originalMessage;
	
	public ResultSkeleton(Message originalMessage, RDPResult result, RDPContainer<?> container) {
		this.originalResult = result;
		this.container = container;
		this.originalMessage = originalMessage;
	}
	
}
