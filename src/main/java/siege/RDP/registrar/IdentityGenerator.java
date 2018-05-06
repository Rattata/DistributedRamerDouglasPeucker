package siege.RDP.registrar;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IdentityGenerator extends UnicastRemoteObject implements IIDGenerationService {

	public IdentityGenerator() throws RemoteException {
	}

	private int currentId = 0;
	private ReentrantLock lock = new ReentrantLock();

	public List<Integer> getRange(int number) throws RemoteException {
		lock.lock();
		List<Integer> returnIdentities = IntStream.rangeClosed(currentId, currentId + number).boxed()
				.collect(Collectors.toList());
		currentId += number;
		lock.unlock();
		return returnIdentities;

	}

	public Integer next() throws RemoteException{
		Integer retVal = null;
		lock.lock();
		retVal = currentId++;
		lock.unlock();
		return retVal;
	}

}
