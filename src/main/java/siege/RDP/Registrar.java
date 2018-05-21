package siege.RDP;

import com.google.inject.Guice;
import com.google.inject.Injector;

import siege.RDP.config.RemoteConfig;
import siege.RDP.data.RMIManager;
import siege.RDP.registrar.IRDPRepository;
import siege.RDP.registrar.IRDPService;
import siege.RDP.registrar.RDPService;
import siege.RDP.registrar.RegistrarRunner;

/**
 * Hello world!
 *
 */
public class Registrar implements IRDPMode
{
	public static Injector injector;
    public static void main( String[] args )
    {
		Registrar.injector = Guice.createInjector(new RegistrarContainer(new Registrar()));
		RegistrarRunner runner = Registrar.injector.getInstance(RegistrarRunner.class);
    }
    
    @Override
	public RDPMode rdpMode() {
		return RDPMode.REGISTRAR;
	}
}
