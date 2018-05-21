package siege.RDP.solver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import siege.RDP.domain.Line;
import siege.RDP.domain.RDPIteration;

public class RDPIterateStrategy implements IRDPstrategy {

	@Override
	public RDPIteration solve(Line line, double epsilon, ISearchStrategy search) {
		List<Integer> results = new ArrayList<>();
		
		LinkedList<Line> workStack = new LinkedList<>();
		workStack.push(line);
		workStack.addLast(line);
		
		while(! workStack.isEmpty()){
			Line tempLine = workStack.pop();
			SearchResult maxIndex = search.findMaximum(tempLine);
			if(maxIndex.furthestDistance >= epsilon){
				for (Line newLine : tempLine.split(maxIndex.furthestIndex)) {
					workStack.addLast(newLine);
				}
			} else {
				results.add(tempLine.start.getIndex());
			}
		}
		return new RDPIteration(new ArrayList<>(), results);
	}
}
