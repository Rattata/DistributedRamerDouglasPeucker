package siege.RDP.data;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Logger;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.Line;
import siege.RDP.messages.RDPWork;

public class WorkSkeleton {
	public Line line;
	public RDPWork work;
	public Double epsilon;
	public String PartitionID;
	public Message originalMessage;
	
	Logger log = Logger.getLogger(this.getClass());
	
	public WorkSkeleton(Message originalMessage, RDPWork work) {
		this.work = work;
		this.originalMessage = originalMessage;
		try {
			this.PartitionID = originalMessage.getStringProperty("JMSXGroupID");
		} catch (JMSException e) {
			log.fatal(e);
			e.printStackTrace();
		}
	}
	
	public WorkSkeleton setLine(List<IOrderedPoint> segment){
		this.line = new Line(segment);
		return this;
	}
	
	public WorkSkeleton setEpsilon(double epsilon){
		this.epsilon = epsilon;
		return this;
	}
}
