package siege.RDP.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.registrar.IRDPRepository;

@Singleton
public class RDPCache implements IRDPCache {

	private HashMap<Integer, TreeMap<Integer, IOrderedPoint>> points = new HashMap<Integer, TreeMap<Integer, IOrderedPoint>>();
	private HashMap<Integer, Double> epsilonStore = new HashMap<>();

	private IRDPRepository lineRepo;

	@Inject
	public RDPCache(RMIManager rmiMan) {
		this.lineRepo = rmiMan.getRepository();
	}
	
	/// get or create if RDP calculation
	// get or create segment of RDP calculation
	@Override
	public List<IOrderedPoint> getSegment(int RDPID, int start, int end) {
		TreeMap<Integer, IOrderedPoint> temp_segment;
		if (points.containsKey(RDPID)) {
			temp_segment = points.get(RDPID);
		} else {
			temp_segment = new TreeMap<Integer, IOrderedPoint>();
			points.put(RDPID, temp_segment);
		}

		List<IOrderedPoint> segment = new ArrayList<IOrderedPoint>(
				temp_segment.subMap(start, true, end, true).values());
		if (segment.size() < end - start) {
			try {
				for (IOrderedPoint wrap : lineRepo.getSegment(RDPID, start, end)) {
					temp_segment.put(wrap.getIndex(), wrap);
				}
				;
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			segment = new ArrayList<IOrderedPoint>(temp_segment.subMap(start, true, end, true).values());
		}
		return segment;
	}

	@Override
	public void invalidate(int RDPID) {
		points.remove(RDPID);
	}

	private static Comparator<IOrderedPoint> pointcomparator = new Comparator<IOrderedPoint>() {
		@Override
		public int compare(IOrderedPoint o1, IOrderedPoint o2) {
			return o1.getIndex() - o2.getIndex();
		}
	};

	@Override
	public double getEpsilon(int RDPID) {
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
