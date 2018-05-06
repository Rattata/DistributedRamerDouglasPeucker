 package siege.RDP.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import siege.RDP.config.RemoteConfig;

@Singleton
public class MessagingFactory implements IMessagingFactory {

	
	private Session session;

	private Logger log = Logger.getLogger(MessagingFactory.class);
	
	private HashMap<String, Destination> queues = new HashMap<>();
	private HashMap<String, Topic> topics = new HashMap<>();
	
	@Inject
	public MessagingFactory(RemoteConfig remoteConfig) {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(remoteConfig.ACTIVEMQ_USER,
					remoteConfig.ACTIVEMQ_PASSWORD, remoteConfig.ACTIVEMQ_URL);
			connectionFactory.setTrustAllPackages(true);
			Connection connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		} catch (Exception e) {
			log.fatal(e);
		}
	}
	
	private Destination getDestination(String destination){
		Destination dest = queues.get(destination);
		if(dest == null){
			try {
				dest = session.createQueue(destination);
				queues.put(destination, dest);				
			} catch (Exception e) {
				log.fatal(e);
			}
		}
		return dest;
	}
	
	@Override
	public MessageProducer createMessageProducer(String queue) {
		Destination dest = getDestination(queue);
		MessageProducer producer = null;
		try {
			producer = session.createProducer(dest);
		} catch (Exception e) {
			log.fatal(e);
		}
		return producer;
	}

	@Override
	public MessageConsumer createMessageConsumer(String queue) {
		Destination dest = getDestination(queue);
		MessageConsumer consumer = null;
		try {
			consumer = session.createConsumer(dest);
		} catch (Exception e) {
			log.fatal(e);
		}
		return consumer;
	}

	public Destination GetTopic(String topicName){
		Topic topicDest = topics.get(topicName);
		if(topicDest == null) {
			try {
				topicDest = session.createTopic(topicName);
				topics.put(topicName, topicDest);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		return topicDest;
	}
	
	@Override
	public MessageProducer createTopicProducer(String topic) {
		Destination dest = GetTopic(topic);
		MessageProducer producer = null;
		try {
			producer =  session.createProducer(dest);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return producer;
	}

	@Override
	public MessageConsumer createTopicConsumer(String topic) {
		Destination dest = GetTopic(topic);
		MessageConsumer consumer = null;
		try {
			consumer =  session.createConsumer(dest);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return consumer;
	}

	@Override
	public ObjectMessage createObjectMessage(Serializable object) {
		ObjectMessage message = null;
		try {
			message = session.createObjectMessage(object);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return message;
	}
	

	
}
