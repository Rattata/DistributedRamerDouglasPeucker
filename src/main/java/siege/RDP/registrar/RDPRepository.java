package siege.RDP.registrar;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;

@Singleton
public class RDPRepository extends UnicastRemoteObject implements IRDPRepository {
	private HashMap<Integer, RDPContainer<?>> store = new HashMap<>();
	
	
	private IdentityGenerator rdp_ids;
	
	private transient Logger log = Logger.getLogger(RDPRepository.class); 
	
	@Inject
	public RDPRepository(IdentityGenerator rdp_ids) throws RemoteException {
		this.rdp_ids = rdp_ids;
		log.info("starting RDPRepo");
	}
	
	private RDPContainer<?> getContainer(int RDPID){
		RDPContainer<?> container = store.get(RDPID);
		if(container == null){
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
		int newID = rdp_ids.next();
		RDPContainer<P> new_container = new RDPContainer<P>(newID, epsilon, points);
		store.put(newID, new_container);
		return new_container;
	}

	@Override
	public void removeContainer(int RDPcontainer) {
		store.remove(RDPcontainer);
	}

	@Override
	public boolean update(int RDPID, int SegmentID, List<Integer> newSegments, List<Integer> newResults)
			throws RemoteException {
		return store.get(RDPID).putResult(SegmentID, newSegments, newResults);
	}

}
