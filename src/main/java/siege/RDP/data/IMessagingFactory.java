package siege.RDP.data;

import java.io.Serializable;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;

import org.apache.activemq.ActiveMQSession;

public interface IMessagingFactory {
	ActiveMQSession getSession();	
	
}
