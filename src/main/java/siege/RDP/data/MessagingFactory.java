 package siege.RDP.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.ActiveMQSession;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import siege.RDP.config.RemoteConfig;

@Singleton
public class MessagingFactory implements IMessagingFactory {

	
	private Logger log = Logger.getLogger(MessagingFactory.class);
	private ActiveMQConnection connection;
	
	@Inject
	public MessagingFactory(RemoteConfig remoteConfig) {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(remoteConfig.ACTIVEMQ_USER,
					remoteConfig.ACTIVEMQ_PASSWORD, remoteConfig.ACTIVEMQ_URL);
			connectionFactory.setTrustAllPackages(true);
			connectionFactory.setAlwaysSessionAsync(true);
			ActiveMQPrefetchPolicy prefetch = new ActiveMQPrefetchPolicy();
			connectionFactory.setPrefetchPolicy(prefetch);
			connection = (ActiveMQConnection) connectionFactory.createConnection();
			connection.start();
			
		} catch (Exception e) {
			log.fatal(e);
		}
	}
	
	
	public ActiveMQSession getSession(){
		try {
			ActiveMQSession session = (ActiveMQSession) connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			return session;
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
			return null;
		}
	}
	
	
}
