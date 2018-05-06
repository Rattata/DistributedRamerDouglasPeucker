package siege.RDP.data;

import java.io.Serializable;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;

public interface IMessagingFactory {
	
	
	MessageProducer createMessageProducer(String queue);
	MessageConsumer createMessageConsumer(String queue);
	MessageProducer createTopicProducer(String topic);
	MessageConsumer createTopicConsumer(String topic);
	ObjectMessage createObjectMessage(Serializable object); 
	
	
}
