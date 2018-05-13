package siege.RDP.messages;

import java.io.Serializable;

public class RDPWork extends IdentifiableMessage {
	public int RDPId;
	public int parentSegmentID;
	public int segmentID;
	public int segmentStartIndex;
	public int endIndex;
	
	public RDPWork(int RDPId, int segmentID, int parentSegmentID, int segmentStartIndex, int endIndex) {
		 this.RDPId = RDPId;
		 this.segmentID = segmentID;
		 this.segmentStartIndex = segmentStartIndex;
		 this.endIndex = endIndex;
		 this.parentSegmentID = parentSegmentID;
		 
	}
	
	@Override
	public String Identifier(){
		return String.format("%d:%d:%d", RDPId, parentSegmentID, segmentID);
	}
}
