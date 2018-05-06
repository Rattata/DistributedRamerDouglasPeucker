package siege.RDP.config;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

public class NodeConfigManager {
	private final String node_config_filename = "node.xml";
	private Logger log = Logger.getLogger(this.getClass());
	
	public NodeConfig GetConfig(){
		File config = getConfigFile();
		if(!config.exists()){
			WriteConfig(new NodeConfig());
		}
		return ReadConfig();
		
	}

	public NodeConfig ReadConfig() {
		File config_file = getConfigFile();
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(NodeConfig.class);
			Unmarshaller unmarshal = jaxbContext.createUnmarshaller();
			return (NodeConfig) unmarshal.unmarshal(config_file);
		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}

	private File getConfigFile() {
		String path = (new File("")).getAbsolutePath() + "\\" + node_config_filename;
		return new File(path);
	}

	public void WriteConfig(NodeConfig cfg) {
		File config_file = getConfigFile();
		NodeConfig default_config = cfg;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(NodeConfig.class);

			if (config_file.exists()) {
				config_file.delete();
			}
			config_file.createNewFile();
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(default_config, config_file);

		} catch (Exception e) {
			log.error(e);
			
		}

	}
}
