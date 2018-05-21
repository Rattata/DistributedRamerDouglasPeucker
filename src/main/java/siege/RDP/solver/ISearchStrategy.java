package siege.RDP.solver;

import siege.RDP.domain.Line;

public interface ISearchStrategy {
	SearchResult findMaximum(Line line);
}
