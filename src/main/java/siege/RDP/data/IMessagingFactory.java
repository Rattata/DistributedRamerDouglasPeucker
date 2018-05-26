package siege.RDP.data;

import org.apache.activemq.ActiveMQSession;

public interface IMessagingFactory {
	ActiveMQSession getSession();	
	
}
