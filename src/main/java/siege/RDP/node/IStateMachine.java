package siege.RDP.node;

import java.util.concurrent.Callable;

public interface IStateMachine extends Callable<Void>{
	void stop();
}
