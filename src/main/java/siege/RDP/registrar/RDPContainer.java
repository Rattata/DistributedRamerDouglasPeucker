package siege.RDP.registrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;
import siege.RDP.domain.PointWrapper;

public class RDPContainer <P extends IPoint> implements IRDPResultContainer {
	
	private int countOpenSegments = 0;
	private ReentrantLock resultcountLock = new ReentrantLock();
	
	private ReentrantLock finalResult = new ReentrantLock();
	
	private int id;
	private double epsilon;
	private List<P> points;
	private List<IOrderedPoint> ordered;
	private TreeMap<Integer, P> results = new TreeMap<>();
	
	private HashMap<Integer, Boolean> expect = new HashMap<Integer, Boolean>();
	
	public RDPContainer(int id, double epsilon, List<P> points){
		this.id = id;
		this.epsilon = epsilon;
		ordered = IntStream.range(0, points.size())
				.mapToObj(x -> new PointWrapper<P>(points.get(x), x))
				.collect(Collectors.toList());
		this.points = points;
		results.put(points.size() - 1, points.get(points.size() - 1));
		expectResult(0);
		finalResult.lock();
	}
	

	public int getId() {
		return id;
	}
	
	public double getEpsilon() {
		return epsilon;
	}
	
	
	public void expectResult(int startIndex) {
		resultcountLock.lock();
		countOpenSegments++;
		expect.put(startIndex, false);
		resultcountLock.unlock();
	}

	/**
	 * @return whether expectation was already received
	 */
	public boolean putResult(int segmentStartId, int[] pointIndices) {
		resultcountLock.lock();
		boolean expectationExists = false; 
		if(expect.containsKey(segmentStartId)){			
			expectationExists = true;
			countOpenSegments--;
			for(int index : pointIndices){
				results.put(index, points.get(index));				
			}
			expect.remove(pointIndices);
			if(countOpenSegments == 0){
				finalResult.unlock();
			}
		}
		resultcountLock.unlock();
		return expectationExists;
	}	

	@SuppressWarnings("unchecked")
	public List<IOrderedPoint> getSegment(int start, int end) {
		return new ArrayList<>(ordered.subList(start, end + 1));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<P> getResult() {
		return new ArrayList<P>(results.values());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<P> awaitResult() {
		finalResult.lock();
		finalResult.unlock();
		return getResult();		
	}
}
