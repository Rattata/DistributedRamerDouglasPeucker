package siege.RDP.config;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RemoteConfig {

	public String ACTIVEMQ_URL = "failover:(tcp://192.168.1.60:61616,localhost:8161)";
	public String ACTIVEMQ_USER = "admin";
	public String ACTIVEMQ_PASSWORD = "admin";

	public String REGISTRATION_MASTER = "192.168.1.60";
	public int REGISTRATION_PORT = 1200;

	public int NODE_UPDATE_PORT = 1201;

	public String RMI_REGISTRAR_REGISTRAR = "REGISTRAR";
	public String RMI_REGISTRAR_LINEREPO = "LINE_REPOSITORY";
	public String RMI_NODE_UPDATE = "UPDATENODE";
	public String RMI_IDGEN = "IDGEN";

	public String QUEUE_WORK = "1_LINES";
	public String QUEUE_RESULTS = "2_RESULTS";
	public String TOPIC_CLEANUP = "3_CLEANUP";

	private static final String remote_config_filename = "remote.xml";

	public static RemoteConfig ReadConfig() {
		File config_file = getConfigFile();
		System.out.println("reading remoteconfig from: "+ config_file.getAbsolutePath());
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(RemoteConfig.class);
			Unmarshaller unmarshal = jaxbContext.createUnmarshaller();
			return (RemoteConfig) unmarshal.unmarshal(config_file);
		} catch (Exception e) {
			RemoteConfig.WriteConfig(new RemoteConfig());
			e.printStackTrace();
			return ReadConfig();
		}
	}

	private static File getConfigFile() {
		File file = new File(remote_config_filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public static void WriteConfig(RemoteConfig cfg) {
		File config_file = getConfigFile();
		RemoteConfig default_config = cfg;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(RemoteConfig.class);

			if (config_file.exists()) {
				config_file.delete();
			}
			config_file.createNewFile();
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(default_config, config_file);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
