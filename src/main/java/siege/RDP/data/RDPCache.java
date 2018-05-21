package siege.RDP.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.registrar.IRDPRepository;

@Singleton
public class RDPCache implements IRDPCache {

	private HashMap<Integer, TreeMap<Integer, IOrderedPoint>> points = new HashMap<Integer, TreeMap<Integer, IOrderedPoint>>();
	private HashMap<Integer, Double> epsilonStore = new HashMap<>();

	private IRDPRepository lineRepo;
	private Logger log = Logger.getLogger(this.getClass());

	@Inject
	public RDPCache(RMIManager rmiMan) {
		this.lineRepo = rmiMan.getRepository();
	}

	private ReentrantLock lock = new ReentrantLock();
	/// get or create if RDP calculation
	// get or create segment of RDP calculation
	@Override
	public List<IOrderedPoint> getSegment(int RDPID, int start, int end) {
		log.info(String.format("getSegment: %d:%d-%d", RDPID, start, end));
		lock.lock();
		TreeMap<Integer, IOrderedPoint> temp_segment;
		if (points.containsKey(RDPID)) {
			temp_segment = points.get(RDPID);
		} else {
			log.info(String.format("create: %d:%d-%d", RDPID, start, end));
			temp_segment = new TreeMap<Integer, IOrderedPoint>();
			points.put(RDPID, temp_segment);
		}

		List<IOrderedPoint> segment = new ArrayList<IOrderedPoint>(
				temp_segment.subMap(start, true, end, true).values());
		if (segment.size() < (end - start)) {
			log.info(String.format("lineRepo: %d:%d-%d", RDPID, start, end));
			
			try {
				for (IOrderedPoint wrap : lineRepo.getSegment(RDPID, start, end)) {
					temp_segment.put(wrap.getIndex(), wrap);
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
			segment = new ArrayList<IOrderedPoint>(temp_segment.subMap(start, true, end, true).values());
		}
		lock.unlock();
		return segment;
	}
	
	@Override
	public void invalidate(int RDPID) {
		points.remove(RDPID);
		epsilonStore.remove(RDPID);
	}

	
	@Override
	public Double getEpsilon(int RDPID) {
		Double epsilon = epsilonStore.get(RDPID);
		if (epsilon == null) {
			try {
				epsilon = lineRepo.getEpsilon(RDPID);
				epsilonStore.put(RDPID, epsilon);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return epsilon;
	}

}
