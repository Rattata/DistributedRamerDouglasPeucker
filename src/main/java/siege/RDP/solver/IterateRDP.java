package siege.RDP.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import siege.RDP.domain.Line;
import siege.RDP.domain.RDPIteration;

public class IterateRDP implements IRDPstrategy {

	@Override
	public RDPIteration solve(Line line, double epsilon, ISearchStrategy search) {
		List<Integer> results = new ArrayList<>();
		List<Line> lines = new ArrayList<>();
		
		SearchResult maxIndex = search.findMaximum(line);
		if(maxIndex.furthestDistance >= epsilon){
			lines.addAll(line.split(maxIndex.furthestIndex));
		} else {
			results.add(line.start.getIndex());
		} 
		return new RDPIteration(lines, results);
	}
}
