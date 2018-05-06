package siege.RDP.messages;

import java.io.Serializable;

public abstract class IdentifiableMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String Identifier(){
		return "";
	}
}
