package siege.RDP.registrar;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import javax.jms.Message;

import siege.RDP.data.IRDPCache;
import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;
import siege.RDP.messages.RDPResult;

public interface IRDPRepository extends Remote, IRDPCache{
	
	void update(RDPResult result, Message message) throws RemoteException;
	
	<P extends IPoint> ICalculationContainer<P> submitCalculation(List<P> points, double epsilon)throws RemoteException;
	
	/**
	 * 
	 * @param segment
	 * @return segmentID
	 */
	int ExpectSegment(int RDPID, List<IOrderedPoint> segment) throws RemoteException;
	
}
