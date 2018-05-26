package siege.RDP.data;

import javax.jms.Message;

import siege.RDP.messages.RDPResult;
import siege.RDP.registrar.ICalculationContainer;

public class ResultSkeleton {
	public RDPResult originalResult;
	public ICalculationContainer<?> container;
	public Message originalMessage;
	
	public ResultSkeleton(Message originalMessage, RDPResult result, ICalculationContainer<?> container) {
		this.originalResult = result;
		this.container = container;
		this.originalMessage = originalMessage;
	}
	
}
