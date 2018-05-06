package siege.RDP.node;

import java.rmi.Remote;
import java.rmi.RemoteException;

import siege.RDP.config.NodeConfig;

public interface IUpdatableNode extends Remote {
	public void update(NodeConfig update) throws RemoteException;
}
