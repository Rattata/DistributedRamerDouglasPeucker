package siege.RDP.messages;

import java.io.Serializable;

public class RDPResult extends IdentifiableMessage {
	public int RDPId;
	public int segmentStartIndex;
	public int[] segmentResultIndices;
	
	public RDPResult(int RDPID, int segmentStartIndex, int[] segmentResultIndices) {
		this.RDPId = RDPID;
		this.segmentResultIndices = segmentResultIndices;
		this.segmentStartIndex = segmentStartIndex;
	}

	@Override
	public String Identifier() {
		return String.format("%d:%d", RDPId, segmentStartIndex);
	}
}
