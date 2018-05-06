package siege.RDP.messages;

import java.io.Serializable;

public class RDPClean extends IdentifiableMessage{
	public int RDPId;
	
	public RDPClean(int RDPId) {
		this.RDPId = RDPId;
	}
}
