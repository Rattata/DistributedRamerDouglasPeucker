package siege.RDP;

import com.google.inject.Guice;
import com.google.inject.Injector;

import siege.RDP.data.RMIManager;

public class Client implements IRDPMode {
	public Injector injector;
	private static Client client;
	RMIManager man;
	
	private Client() {

	}

	
	public static Client getClient(){
		Client.client = new Client();
		client.injector = Guice.createInjector(new ClientContainer(client));
		client.man = client.injector.getInstance(RMIManager.class);
		return Client.client;
	};

	@Override
	public RDPMode rdpMode() {
		return RDPMode.CLIENT;
	}
	
	
	
	
}
