package siege.RDP.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import siege.RDP.domain.Line;
import siege.RDP.domain.RDPIteration;

public class RDPCompleteStrategy implements IRDPstrategy {

	private Logger log = Logger.getLogger(this.getClass());

	@Override
	public RDPIteration solve(Line line, double epsilon, ISearchStrategy search) {
		List<Integer> results = new ArrayList<>();
		
		Stack<Line> workStack = new Stack<>();
		workStack.push(line);
		int iterations = 0;
		while(! workStack.isEmpty()){
			iterations++;
			Line tempLine = workStack.pop();
			SearchResult maxIndex = search.findMaximum(tempLine);
			if(maxIndex.furthestDistance >= epsilon){
				workStack.addAll(tempLine.split(maxIndex.furthestIndex));
			} else {
				results.add(tempLine.start.getIndex());
			}
		}
		log.debug(String.format("solve completed in %d iterations", iterations));
		return new RDPIteration(new ArrayList<>(), results);
	}
}
