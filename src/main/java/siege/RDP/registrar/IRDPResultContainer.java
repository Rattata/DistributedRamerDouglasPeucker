package siege.RDP.registrar;


import java.rmi.Remote;
import java.util.List;

import siege.RDP.domain.IPoint;

public interface IRDPResultContainer extends Remote {
	void expectResult(int segmentStartIndex);
	boolean putResult(int segmentStartIndex, int[] segmentPoints);
	<P extends IPoint> List<P> getResult();
	<P extends IPoint> List<P> awaitResult();
}
