package siege.RDP.registrar;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.google.inject.name.Named;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;

@Singleton
public class RDPRepository extends UnicastRemoteObject implements IRDPRepository {
	private HashMap<Integer, RDPContainer<?>> store = new HashMap<>();

	private IdentityGenerator rdp_ids;
	private IdentityGenerator segment_ids;

	private transient Logger log = Logger.getLogger(RDPRepository.class);

	@Inject
	public RDPRepository(@Named("RDP") IdentityGenerator rdp_ids, @Named("Segment") IdentityGenerator segment_ids)
			throws RemoteException {
		this.rdp_ids = rdp_ids;
		this.segment_ids = segment_ids;
		log.info("starting RDPRepo");
	}

	private RDPContainer<?> getContainer(int RDPID) {
		RDPContainer<?> container = store.get(RDPID);
		if (container == null) {
			log.error(String.format("container %d not found ", RDPID));
		}
		return container;
	}

	@Override
	public List<IOrderedPoint> getSegment(int RDPID, int start, int end) throws RemoteException {
		RDPContainer<?> container = getContainer(RDPID);
		return container.getSegment(start, end);
	}

	@Override
	public Double getEpsilon(int RDPID) throws RemoteException {
		return store.get(RDPID).getEpsilon();
	}

	@Override
	public <P extends IPoint> RDPContainer<P> submit(List<P> points, double epsilon) {
		try {
			int newRdpID = rdp_ids.next();
			int newSegID = segment_ids.next();
			RDPContainer<P> new_container = new RDPContainer<P>(newRdpID, newSegID, epsilon, points);
			store.put(newRdpID, new_container);
			return new_container;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void removeContainer(int RDPcontainer) {
		store.remove(RDPcontainer);
	}

	@Override
	public boolean update(int RDPID, int SegmentID, List<Integer> newSegments, List<Integer> newResults)
			throws RemoteException {
		return store.get(RDPID).update(SegmentID, newSegments, newResults);
	}

}
