package siege.RDP.messages;

import java.util.List;

public class RDPResult extends IdentifiableMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -781145430929939467L;
	public int RDPId;
	public int ParentSegmentID;
	public int SegmentID;
	public List<Integer> newSegments;
	public List<Integer> segmentResultIndices;
	
	public RDPResult(int RDPID, int SegmentID, int ParentSegmentID, List<Integer> newSegments, List<Integer> segmentResultIndices) {
		this.RDPId = RDPID;
		this.newSegments = newSegments;
		this.segmentResultIndices = segmentResultIndices;
		this.SegmentID = SegmentID;
		this.ParentSegmentID = ParentSegmentID;
	}

	@Override
	public String Identifier() {
		return String.format("%d:%s", RDPId, SegmentID);
	}
}
