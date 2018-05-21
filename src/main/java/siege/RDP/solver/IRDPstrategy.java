package siege.RDP.solver;

import siege.RDP.domain.Line;
import siege.RDP.domain.RDPIteration;

public interface IRDPstrategy {
	RDPIteration solve(Line line, double epsilon, ISearchStrategy searchStrategy);
}
