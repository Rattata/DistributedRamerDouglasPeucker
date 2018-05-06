package siege.RDP.registrar;

import java.rmi.RemoteException;
import java.util.List;

import com.google.inject.Inject;

import siege.RDP.domain.IPoint;

public class RDPService implements IRDPService {
	
	@Inject
	public RDPService() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public <P extends IPoint> List<P> submit(List<P> points, double epsilon) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
 
}
