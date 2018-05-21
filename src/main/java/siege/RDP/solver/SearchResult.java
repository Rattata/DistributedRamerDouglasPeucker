package siege.RDP.solver;

public class SearchResult implements Comparable<SearchResult>{
	public int furthestIndex = -1;
	public double furthestDistance = Double.MIN_VALUE; 
	
	public SearchResult(int furthestIndex, double furthestDistance) {
		this.furthestDistance = furthestDistance;
		this.furthestIndex = furthestIndex;
	}

	@Override
	public int compareTo(SearchResult o) {
		if(o.furthestDistance > furthestDistance) return -1;
		if(o.furthestDistance < furthestDistance) return 1;
		return 0;
	}
}
