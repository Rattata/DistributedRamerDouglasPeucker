package siege.RDP.registrar;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;

@Singleton
public class RDPRepository implements IRDPRepository {
	private HashMap<Integer, RDPContainer> store = new HashMap<>();
	
	
	private IdentityGenerator rdp_ids;
	
	private transient Logger log = Logger.getLogger(RDPRepository.class); 
	
	@Inject
	public RDPRepository(IdentityGenerator rdp_ids) {
		this.rdp_ids = rdp_ids;
		log.info("starting RDPRepo");
	}
	
	private RDPContainer getContainer(int RDPID){
		RDPContainer container = store.get(RDPID);
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
	public void signalExpectation(int RDPID, int segmentStartIndex) throws RemoteException {
		getContainer(RDPID).expectResult(segmentStartIndex);
	}

	@Override
	public boolean finalizeExpectation(int RDPID, int segmentStartIndex, int[] segmentResultIndices) {
		return getContainer(RDPID).putResult(segmentStartIndex, segmentResultIndices);
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

}
