package siege.RDP.registrar;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import org.apache.activemq.ActiveMQSession;
import org.apache.log4j.Logger;

import com.google.inject.name.Named;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.MessagingFactory;
import siege.RDP.data.ResultSkeleton;
import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;
import siege.RDP.messages.RDPClean;
import siege.RDP.messages.RDPResult;
import siege.RDP.messages.RDPWork;

@Singleton
public class RDPRepository extends UnicastRemoteObject implements IRDPRepository {
	private HashMap<Integer, ICalculationContainer<?>> store = new HashMap<>();

	private IIDGenerationService rdp_ids;
	private IIDGenerationService segment_ids;
	
	private ExecutorService executor;

	private transient Logger log = Logger.getLogger(this.getClass());

	@Inject
	public RDPRepository(@Named("RDP") IIDGenerationService rdp_ids, @Named("Segment") IIDGenerationService segment_ids, RemoteConfig rconfig, ExecutorService executor)
			throws RemoteException {
		this.rdp_ids = rdp_ids;
		this.segment_ids = segment_ids;
		this.executor = executor;
		
		log.info("starting RDPRepo");
		
	}

	private ICalculationContainer<?> getContainer(int RDPID) {
		ICalculationContainer<?> container = store.get(RDPID);
		if (container == null) {
			log.error(String.format("container %d not found ", RDPID));
		}
		return container;
	}

	@Override
	public List<IOrderedPoint> getSegment(int RDPID, int start, int end) throws RemoteException {
		log.info(String.format("getSegment: %d:%d-%d", RDPID, start, end));
		ICalculationContainer<?> container = getContainer(RDPID);
		return container.getSegment(start, end);
	}

	@Override
	public Double getEpsilon(int RDPID) throws RemoteException {
		log.info(String.format("getEpsilon: %d", RDPID));
		return store.get(RDPID).getEpsilon();
	}

	@Override
	public <P extends IPoint> ICalculationContainer<P> submitCalculation(List<P> points, double epsilon) {
		try {
			int newRdpID = rdp_ids.next();
			ICalculationContainer<P> new_container = new RDPContainer<P>(newRdpID, epsilon, points);
			store.put(newRdpID, new_container);
			return new_container;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	
	@Override
	public void update(RDPResult result, Message message) throws RemoteException {
		ICalculationContainer<?> container = store.get(result.RDPId);
		
		CompletableFuture<ResultSkeleton> updateFuture = CompletableFuture.completedFuture(new ResultSkeleton( message, result,container));
		
		updateFuture.thenApplyAsync((xskelet) -> setResult(xskelet), executor);
		updateFuture.thenApplyAsync((skeleton) -> setUpdate(skeleton), executor);
		updateFuture.thenAccept((x) -> {
			try {
				x.originalMessage.acknowledge();
			} catch (JMSException e) {
				log.fatal(e);
				e.printStackTrace();
			}
		});
		CompletableFuture.supplyAsync(() -> updateFuture.join(), executor);
	}

	
	private ResultSkeleton setResult(ResultSkeleton skeleton){
		skeleton.container.putResults(skeleton.originalResult.segmentResultIndices);
		return skeleton;
	}
	
	
	private ResultSkeleton setUpdate(ResultSkeleton skeleton){
		RDPResult result = skeleton.originalResult;
		skeleton.container.update(result.SegmentID, result.ParentSegmentID, result.newSegments);
		return skeleton;
	}

	@Override
	public void invalidate(int RDPID) throws RemoteException {
		store.remove(RDPID);
	}

	@Override
	public int ExpectSegment(int RDPID, List<IOrderedPoint> segment) {
		int newSegmentID = 0;
		try {
			newSegmentID = segment_ids.next();
			store.get(RDPID).Expect(newSegmentID);
		} catch (RemoteException e) {
			log.fatal(e);
			e.printStackTrace();
		}
		return newSegmentID;
	}
	
	
}
