package siege.RDP.domain;

import java.util.List;

public class RDPIteration {
	List<Line> newLines;
	List<Integer> resultPoints;
	
	public RDPIteration(List<Line> newLines, List<Integer> resultPoints) {
		this.newLines = newLines;
		this.resultPoints = resultPoints;
	}
}
