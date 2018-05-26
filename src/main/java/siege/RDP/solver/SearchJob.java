package siege.RDP.solver;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.Line;
import siege.RDP.messages.IdentifiableMessage;

public class SearchJob extends IdentifiableMessage implements Callable<SearchResult> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2191706176151277115L;
	public Line line;
	public int start;
	public int end;
	
	public int furthestIndex = -1;
	public double furthestDistance = Double.MIN_VALUE;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public SearchJob(Line line, int start, int end) {
		this.line = line;
		this.start = start;
		this.end = end;
	}
	
	
	public SearchResult call() throws Exception{
		List<IOrderedPoint> points = line.getPoints();
		for(int index = start;index <= end && index < points.size(); index++){
			double distance = line.distance(points.get(index));
			if(distance > furthestDistance){
				furthestDistance = distance;
				furthestIndex = index;
			}
		}
		return new SearchResult(furthestIndex, furthestDistance );
	}
	
	
	@Override
	public String Identifier() {
		return String.format("%d:%d", start,end);
	}
}
