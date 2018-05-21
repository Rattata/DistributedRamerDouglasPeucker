package siege.RDP.data;

import java.rmi.RemoteException;
import java.util.List;

import siege.RDP.domain.IOrderedPoint;

public interface IRDPCache{
	List<IOrderedPoint> getSegment(int RDPID, int start, int end) throws RemoteException;
	Double getEpsilon(int RDPID) throws RemoteException;
	void invalidate(int RDPID) throws RemoteException;
}
