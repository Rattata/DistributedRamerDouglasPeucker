package siege.RDP.messages;

import java.io.Serializable;

public class RDPAnnounce extends IdentifiableMessage{
	public int RDPId;
	int length;
	
	public RDPAnnounce(int RDPId, int length) {
		this.RDPId = RDPId;
		this.length = length;
	}
	
	public int start(){
		return 0;
	}
	
	public int end(){
		return length-1;
	}
	
	@Override
	public String Identifier() {
		return String.format("%d", RDPId);
	}
}
