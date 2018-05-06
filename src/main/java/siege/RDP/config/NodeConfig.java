package siege.RDP.config;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeConfig {

	public int consumers = 1;
	public int search_segments = consumers + 1;
	public int split = 125000;
	public int rmi_registry_port = 1201;

	
}
