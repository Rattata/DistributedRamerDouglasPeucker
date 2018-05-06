package siege.RDP.data;

import java.util.List;

import siege.RDP.domain.IOrderedPoint;

public interface IRDPCache{
	List<IOrderedPoint> getSegment(int RDPID, int start, int end);
	double getEpsilon(int RDPID);
	void invalidate(int RDPID);
}
