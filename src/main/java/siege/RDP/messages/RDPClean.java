package siege.RDP.messages;

public class RDPClean extends IdentifiableMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3116460973310877430L;
	public int RDPId;
	
	public RDPClean(int RDPId) {
		this.RDPId = RDPId;
	}
	
	@Override
	public String Identifier() {
		return String.format("%d", RDPId);
	}
}
