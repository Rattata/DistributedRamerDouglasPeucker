package siege.RDP.registrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;
import siege.RDP.domain.PointWrapper;
import siege.RDP.messages.RDPResult;

public class RDPContainer<P extends IPoint> implements IRDPResultContainer {

	private ReentrantLock resultcountLock = new ReentrantLock();

	private CountDownLatch finalResult = new CountDownLatch(1);

	private int id;
	private double epsilon;
	private List<P> points;
	private List<IOrderedPoint> ordered;
	private TreeMap<Integer, P> results = new TreeMap<>();

	private HashMap<Integer, Boolean> expect = new HashMap<>();

	Logger log = Logger.getLogger(this.getClass());

	public RDPContainer(int RDPID, int segmentID, double epsilon, List<P> points) {
		this.id = RDPID;
		this.epsilon = epsilon;
		expect.put(segmentID, false);
		ordered = IntStream.range(0, points.size()).mapToObj(x -> new PointWrapper<P>(points.get(x), x))
				.collect(Collectors.toList());
		this.points = points;
		results.put(points.size() - 1, points.get(points.size() - 1));

	}

	public int getId() {
		return id;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public boolean update(int SegmentID, int ParentSegmentID, List<Integer> newSegments, List<Integer> newResults) {

		// store points
		for (Integer result : newResults) {
			results.put(result, points.get(result));
		}

		resultcountLock.lock();
		manageFam(ParentSegmentID);
		for (Integer integer : newSegments) {
			manageFam(integer);
		}
		
		expect.put(SegmentID, true);
		if(!expect.containsValue(false)){
			finalResult.countDown();
			return true;
		}
		resultcountLock.unlock();
		return false;
	}

	private void manageFam(Integer index){
		if( ( ! expect.containsKey(index) ) && index != -1){
			expect.put(index, false);
		}
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
		try {
			finalResult.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return getResult();
	}
}
