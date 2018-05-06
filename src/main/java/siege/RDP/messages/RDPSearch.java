package siege.RDP.messages;

import java.util.concurrent.Callable;

import javax.print.DocFlavor.STRING;

import org.apache.log4j.Logger;

import siege.RDP.domain.Line;

public class RDPSearch extends IdentifiableMessage implements Callable<Void> {
	public Line line;
	public int start;
	public int end;
	
	public int furthestIndex = -1;
	public double furthestDistance = Double.MIN_VALUE;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public RDPSearch(Line line, int start, int end) {
		this.line = line;
		this.start = start;
		this.end = end;
	}
	
	
	public Void call() throws Exception{
		String identifier = Identifier(); 
		log.info(String.format("%s search start", identifier));
		for(int j = start;j <= end && j < line.getPoints().size(); j++){
			double distance = line.distance(line.getPoints().get(j));
			if(distance > furthestDistance){
				furthestDistance = distance;
				furthestIndex = j;
			}
		}
		log.info(String.format("%s search finish", identifier));
		return null;
	};
	
	@Override
	public String Identifier() {
		return String.format("%d:%d", start,end);
	}
}
