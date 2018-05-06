package siege.RDP.registrar;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;

public interface IRDPRepository extends Remote, Serializable{
	List<IOrderedPoint> getSegment(int RDPID, int start, int end) throws RemoteException;
	
	void signalExpectation(int RDPID, int segmentStartIndex) throws RemoteException;
	
	Double getEpsilon(int RDPID) throws RemoteException; 
	
	boolean finalizeExpectation(int RDPID, int segmentStartIndex, int[] segmentResultIndices);
	
	<P extends IPoint> RDPContainer<P> submit(List<P> points, double epsilon);
	
	void removeContainer(int RDPcontainer);
	
}
