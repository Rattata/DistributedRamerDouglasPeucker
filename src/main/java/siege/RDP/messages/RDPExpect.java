package siege.RDP.messages;

import java.io.Serializable;

public class RDPExpect extends IdentifiableMessage{
	public int RDPID;
	public int ResultStartIndex;
	
	public RDPExpect(int RDPId, int ResultStartIndex) {
		this.RDPID = RDPId;
		this.ResultStartIndex = ResultStartIndex;
	}
	
	@Override
	public String Identifier(){
		return String.format("%d:%d", RDPID, ResultStartIndex);
	}
}
