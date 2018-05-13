package siege.RDP.registrar;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.MessageProducer;

import org.apache.log4j.Logger;

import com.google.inject.name.Named;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.MessagingFactory;
import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;
import siege.RDP.messages.RDPClean;
import siege.RDP.messages.RDPResult;
import siege.RDP.messages.RDPWork;

@Singleton
public class RDPRepository extends UnicastRemoteObject implements IRDPRepository {
	private HashMap<Integer, RDPContainer<?>> store = new HashMap<>();

	private IIDGenerationService rdp_ids;
	private IIDGenerationService segment_ids;
	
	private MessagingFactory messFact;
	private MessageProducer work_producer;

	private transient Logger log = Logger.getLogger(this.getClass());

	@Inject
	public RDPRepository(@Named("RDP") IIDGenerationService rdp_ids, @Named("Segment") IIDGenerationService segment_ids, MessagingFactory messFact, RemoteConfig rconfig)
			throws RemoteException {
		this.rdp_ids = rdp_ids;
		this.messFact = messFact;
		this.segment_ids = segment_ids;
		this.work_producer = messFact.createMessageProducer(rconfig.QUEUE_WORK);
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
			
			RDPWork work = new RDPWork(newRdpID, newSegID, -1,  0, points.size() - 1);
			log.info(String.format("%s wrapped in work object", work.Identifier()));
			
			work_producer.send(messFact.createObjectMessage(work));
			log.info(String.format("%s sent to queue", work.Identifier()));
			
			
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
	public boolean update(RDPResult result) throws RemoteException {
		return store.get(result.RDPId).update(result.SegmentID, result.ParentSegmentID, result.newSegments, result.segmentResultIndices);
	}

}
