package siege.RDP.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeConfig implements Serializable {

	public int consumers = 1;
	public int rmi_registry_port = 1201;
	public int max_partitions = 1;
	public int search_chunk_size = 25000;
	public int cores = Runtime.getRuntime().availableProcessors();
	public boolean useAnnounce = false;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("consumers: %d\t search_chunk_size:%d\t max_partitions:%d",consumers, search_chunk_size, max_partitions);
	}

	
}
