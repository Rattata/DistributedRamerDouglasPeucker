package siege.RDP.messages;

public class RDPAnnounce extends IdentifiableMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 765768673755173180L;
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
