package siege.RDP.registrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;
import siege.RDP.domain.PointWrapper;
import siege.RDP.messages.RDPResult;

public class RDPContainer<P extends IPoint> implements ICalculationContainer<P> {

	private ReentrantLock resultcountLock = new ReentrantLock();
	private ReentrantLock resultTreeLock = new ReentrantLock();
	private CountDownLatch finalResult = new CountDownLatch(1);

	private int RDPId;
	private double epsilon;
	private List<P> points;
	private List<IOrderedPoint> ordered;
	private TreeMap<Integer, P> results = new TreeMap<>();

	private boolean done = false;
	private HashMap<Integer, Boolean> expect ;

	Logger log = Logger.getLogger(this.getClass());
	
	public RDPContainer(int RDPID, double epsilon, List<P> points) {
		this.RDPId = RDPID;
		this.epsilon = epsilon;
		expect = new HashMap<>(40);
		ordered = IntStream.range(0, points.size()).mapToObj(x -> new PointWrapper<P>(points.get(x), x))
				.collect(Collectors.toList());
		this.points = points;
		results = new TreeMap<>();

		results.put(points.size() - 1, points.get(points.size() - 1));
	}

	
	public double getEpsilon() {
		return epsilon;
	}

	@Override
	public boolean update(int SegmentID, int ParentSegmentID, List<Integer> newSegments) {
		log.info(String.format("received update: %d:%d", SegmentID, ParentSegmentID));
		resultcountLock.lock();
		manageFam(ParentSegmentID);
		for (Integer integer : newSegments) {
			manageFam(integer);
		}
		
		expect.put(SegmentID, true);
		if(!expect.containsValue(false)){
			if(done) {
				log.fatal("messager broken, please advice");
			} else {
				log.info(String.format("%d is done!", RDPId));
				done = true;
				finalResult.countDown();
			}
			return true;
		}
		resultcountLock.unlock();
		log.info(String.format("completed update: %d:%d", SegmentID, ParentSegmentID));
		
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
		resultTreeLock.lock();
		List<P> returnresults =  new ArrayList<P>(results.values());
		resultTreeLock.unlock();
		return returnresults;
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

	@Override
	public void putResults(List<Integer> newResults) {
		log.info(String.format("received result"));
		
		resultTreeLock.lock();
		for (Integer result : newResults) {
			results.put(result, points.get(result));
		}
		resultTreeLock.unlock();
		log.info(String.format("completed result update"));
		
	}
	
	@Override
	public void Expect(int segmentID){
		expect.put(segmentID, false);
	}

	@Override
	public List<IOrderedPoint> getLine() {
		return ordered;
	}

	@Override
	public int getRDPId() {
		return RDPId;
	}

}
