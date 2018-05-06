package siege.RDP.messages;

import java.io.Serializable;
import java.util.List;

public class RDPResult extends IdentifiableMessage {
	public int RDPId;
	public int SegmentID;
	public List<Integer> newSegments;
	public List<Integer> segmentResultIndices;
	
	public RDPResult(int RDPID, int SegmentID, List<Integer> newSegments, List<Integer> segmentResultIndices) {
		this.RDPId = RDPID;
		this.newSegments = newSegments;
		this.segmentResultIndices = segmentResultIndices;
		this.SegmentID = SegmentID;
	}

	@Override
	public String Identifier() {
		return String.format("%d:%s", RDPId, SegmentID);
	}
}
