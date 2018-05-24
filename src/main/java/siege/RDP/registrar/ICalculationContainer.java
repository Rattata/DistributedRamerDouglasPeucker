package siege.RDP.registrar;


import java.rmi.Remote;
import java.util.List;

import siege.RDP.domain.IOrderedPoint;
import siege.RDP.domain.IPoint;

public interface  ICalculationContainer<P extends IPoint> extends Remote {
	boolean update(int SegmentID, int ParentSegmentID, List<Integer> newSegments);
	void putResults(List<Integer> newResults);
	
	List<IOrderedPoint> getSegment(int start, int end);
	
	List<IOrderedPoint> getLine();
	
	
	List<P> getResult();
	
	List<P> awaitResult();
	
	double getEpsilon();
	
	void Expect(int segmentID);
	
	int getRDPId();
	
}
