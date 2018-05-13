package siege.RDP.registrar;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;
import siege.RDP.messages.RDPResult;

public interface IRDPRepository extends Remote{
	List<IOrderedPoint> getSegment(int RDPID, int start, int end) throws RemoteException;
	
	Double getEpsilon(int RDPID) throws RemoteException; 
	
	boolean update(RDPResult result) throws RemoteException;
	
	<P extends IPoint> RDPContainer<P> submit(List<P> points, double epsilon)throws RemoteException;
	
	void removeContainer(int RDPcontainer)throws RemoteException;
	
}
