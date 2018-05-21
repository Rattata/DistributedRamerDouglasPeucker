package siege.RDP.registrar;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import siege.RDP.domain.IPoint;

public interface IRDPService extends Remote {
	public <P extends IPoint> List<P> submit(List<P> points, double epsilon) throws RemoteException;
	public <P extends IPoint> List<P> submit(List<P> points, double epsilon, int preSolve) throws RemoteException;
}
