package siege.RDP.solver;

import java.util.ArrayList;
import java.util.List;

import siege.RDP.domain.Line;
import siege.RDP.domain.RDPIteration;

public class RDPIterateStrategy implements IRDPstrategy {

	@Override
	public RDPIteration solve(Line line, double epsilon, ISearchStrategy search) {
		List<Integer> results = new ArrayList<>();
		List<Line> newLines = new ArrayList<>();
		
		SearchResult maxIndex = search.findMaximum(line);
		if(maxIndex.furthestDistance >= epsilon){
			for (Line newLine : line.split(maxIndex.furthestIndex)) {
					newLines.add(newLine);
			}
		} else {
			results.add(line.start.getIndex());
		}
		return new RDPIteration(newLines, results);
	}
}
