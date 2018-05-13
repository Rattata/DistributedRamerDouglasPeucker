package siege.RDP.registrar;


import java.rmi.Remote;
import java.util.List;

import siege.RDP.domain.IPoint;

public interface IRDPResultContainer extends Remote {
	boolean update(int SegmentID, int ParentSegmentID, List<Integer> newSegments, List<Integer> newResults);
	<P extends IPoint> List<P> getResult();
	<P extends IPoint> List<P> awaitResult();
}
