package siege.RDP.registrar;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ISegmentIDGenerator extends Remote {
	List<Integer> getRange(int number) throws RemoteException;
	Integer next() throws RemoteException;
}
