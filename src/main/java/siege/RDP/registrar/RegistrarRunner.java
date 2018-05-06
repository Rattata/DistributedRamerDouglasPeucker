package siege.RDP.registrar;

import com.google.inject.Inject;

public class RegistrarRunner implements Runnable {

	ResultConsumer consumer;
	
	@Inject
	public RegistrarRunner(ResultConsumer consumer) {
		this.consumer = consumer;
	}
	
	@Override
	public void run() {
		while(true){
			consumer.execute();
		}
		
	}
	
}