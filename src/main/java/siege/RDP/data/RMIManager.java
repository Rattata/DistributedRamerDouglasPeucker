package siege.RDP.data;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import siege.RDP.IRDPMode;
import siege.RDP.RDPMode;
import siege.RDP.config.RemoteConfig;
import siege.RDP.node.IUpdatableNode;
import siege.RDP.registrar.IRDPRepository;
import siege.RDP.registrar.IRDPService;
import siege.RDP.registrar.ISegmentIDGenerator;

@Singleton
public class RMIManager {
	private RemoteConfig remote_cfg;
	private HashMap<String, Registry> remotes;
	private Logger log = Logger.getLogger(this.getClass()); 
	private IRDPMode mode;
	
	private Registry localRegistry;
	
	@Inject
	public RMIManager(RemoteConfig remote_cfg, IRDPMode mode) {
		this.remote_cfg = remote_cfg;
		this.remotes = new HashMap<>();
		this.mode = mode;

	}

	private Registry getLocalRegistry(){
		if(localRegistry != null){
			return localRegistry;
		}
		Registry local = null;
		int port = mode.rdpMode() == RDPMode.NODE ? remote_cfg.NODE_UPDATE_PORT : remote_cfg.REGISTRATION_PORT;
		try {
			local = LocateRegistry.createRegistry(port);
		} catch (Exception e) {
			try {
				local = LocateRegistry.getRegistry(port);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			log.info("create registry");
		}
		localRegistry = local;
		return local;
	}
	
	public IRDPRepository getRepository() {

		IRDPRepository rdpRepo = null;
		try {
			Registry r = remotes.get(remote_cfg.REGISTRATION_MASTER);
			if (r == null) {
				r = LocateRegistry.getRegistry(remote_cfg.REGISTRATION_MASTER, remote_cfg.REGISTRATION_PORT);
				remotes.put(remote_cfg.REGISTRATION_MASTER, r);
			}
			Remote obj = r.lookup(remote_cfg.RMI_REGISTRAR_LINEREPO);
			rdpRepo = (IRDPRepository) obj;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rdpRepo;
	}
	
	public IRDPService getRDPService() {

		IRDPService rdpService = null;
		try {
			Registry r = remotes.get(remote_cfg.REGISTRATION_MASTER);
			if (r == null) {
				r = LocateRegistry.getRegistry(remote_cfg.REGISTRATION_MASTER, remote_cfg.REGISTRATION_PORT);
				remotes.put(remote_cfg.REGISTRATION_MASTER, r);
			}
			Remote obj = r.lookup(remote_cfg.RMI_REGISTRAR_REGISTRAR);
			rdpService = (IRDPService) obj;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rdpService;
	}

	public ISegmentIDGenerator getIDGen() {

		ISegmentIDGenerator rdpService = null;
		try {
			Registry r = remotes.get(remote_cfg.REGISTRATION_MASTER);
			if (r == null) {
				r = LocateRegistry.getRegistry(remote_cfg.REGISTRATION_MASTER, remote_cfg.REGISTRATION_PORT);
				remotes.put(remote_cfg.REGISTRATION_MASTER, r);
			}
			Remote obj = r.lookup(remote_cfg.RMI_IDGEN);
			rdpService = (ISegmentIDGenerator) obj;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rdpService;
	}

	
	public IUpdatableNode getUpdatableNode(String node) {

		IUpdatableNode rdpService = null;
		try {
			Registry r = remotes.get(remote_cfg.REGISTRATION_MASTER+String.valueOf(remote_cfg.NODE_UPDATE_PORT));
			if (r == null) {
				r = LocateRegistry.getRegistry(node, remote_cfg.NODE_UPDATE_PORT);
				remotes.put(remote_cfg.REGISTRATION_MASTER+String.valueOf(remote_cfg.NODE_UPDATE_PORT), r);
			}
			Remote obj = r.lookup(remote_cfg.RMI_NODE_UPDATE);
			rdpService = (IUpdatableNode) obj;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rdpService;
	}

	private IUpdatableNode GetUpdatableNode(String remoteIp, int remotePort) {
		Registry remoteRegistry = remotes.get(remoteIp);
		if (remoteRegistry == null) {
			try {
				remoteRegistry = LocateRegistry.getRegistry(remoteIp, remotePort);
				remotes.put(remoteIp, remoteRegistry);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		IUpdatableNode target = null;
		try {
			target = (IUpdatableNode) remoteRegistry.lookup(remote_cfg.RMI_NODE_UPDATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return target;
	}
	
	

	public void RegisterRepository( IRDPRepository obj) {
		Registry r = null;
		try {
			r = getLocalRegistry();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			r.bind(remote_cfg.RMI_REGISTRAR_LINEREPO, obj);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void RegisterIDGenerator( ISegmentIDGenerator obj) {
		Registry r = null;
		try {
			r = getLocalRegistry();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			r.bind(remote_cfg.RMI_IDGEN, obj);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void RegisterRdpService( IRDPService obj) {
		try {
			getLocalRegistry().bind(remote_cfg.RMI_REGISTRAR_REGISTRAR, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void RegisterUpdatableNode( IUpdatableNode obj) {
		try {
			getLocalRegistry().bind(remote_cfg.RMI_NODE_UPDATE, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	

}
