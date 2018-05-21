package siege.RDP.node;

import java.util.concurrent.Callable;

public interface IStateMachine extends Runnable{
	void stop();
}
