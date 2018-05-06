package siege.RDP.messages;

import java.io.Serializable;

public class RDPWork extends IdentifiableMessage {
	public int RDPId;
	public int segmentStartIndex;
	public int endIndex;
	
	public RDPWork(int RDPId, int segmentStartIndex, int endIndex) {
		 this.RDPId = RDPId;
		 this.segmentStartIndex = segmentStartIndex;
		 this.endIndex = endIndex;
	}
	
	@Override
	public String Identifier(){
		return String.format("%d:%d:%d", RDPId, segmentStartIndex, endIndex);
	}
}
