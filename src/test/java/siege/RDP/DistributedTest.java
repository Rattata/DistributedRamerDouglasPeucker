package siege.RDP;

import java.io.BufferedWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.testng.annotations.Test;

import com.google.common.base.Stopwatch;

import static com.google.common.base.Stopwatch.*;

import siege.RDP.config.NodeConfig;
import siege.RDP.domain.PointImpl;
import siege.RDP.node.IUpdatableNode;
import siege.RDP.registrar.IRDPService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;



public class DistributedTest {

	@Test
	public void SimpleRun() {		
		try {
			
			Client client = Client.getClient();
			IRDPService service  = client.man.getRDPService();
			
			List<PointImpl> n = create(100000);
			NodeConfig settings = new NodeConfig();
			
//			IUpdatableNode node1 = client.man.getUpdatableNode("192.168.1.61");
//			IUpdatableNode node2 = ProcessingNode.connect("192.168.1.250");
//			IUpdatableNode node3 = ProcessingNode.connect("192.168.1.250");
			
//			node1.update(settings);
//			node2.update(settings);
//			node2.update(settings);
			
			Stopwatch w = createUnstarted();
			w.start();
			n = service.submit(n, 0.001);
			
			System.out.printf("%d -> %d\n", n.size(), w.stop().elapsed(TimeUnit.NANOSECONDS));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int increment = 1000;
	List<Integer> split_thresholds = Arrays.asList(new Integer[] { 16000, 64000, 128000 });
	List<Triplet<Integer, Integer, Integer>> lineSearchRatio = new ArrayList<Triplet<Integer, Integer, Integer>>();
	private List<Triplet<Integer, Integer, Integer>> createConsumerSets() {
		lineSearchRatio.add(new Triplet<Integer, Integer, Integer>(2, 2, 3));
		// lineSearchRatio.add(new Triplet<Integer, Integer, Integer>(1, 3, 4));
		return lineSearchRatio;
	}

	// splitthreshold
	private List<Tuple<Integer, Triplet<Integer, Integer, Integer>>> newTestSetup() {
		List<Tuple<Integer, Triplet<Integer, Integer, Integer>>> values = new ArrayList<>();
		for (Triplet<Integer, Integer, Integer> consumer : createConsumerSets()) {
			for (Integer split : split_thresholds) {
				values.add(new Tuple<Integer, Triplet<Integer, Integer, Integer>>(split, consumer));
			}

		}
		return values;
	}

	// @Test
	// public void Connect() {
	// try {
	//// RegistrationService resv = ProcessRegistrar.connect();
	// UpdatableNode node = ProcessingNode.connect("192.168.1.250");
	// node.update(new ProcessLineSettings());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	private List<PointImpl> create(int numberOfPoints) {
		// (30x - x^2) * cos(2pi/ 0.5 * x) - 30
		List<PointImpl> points = new ArrayList<PointImpl>();
		double delta = 30 / (double) numberOfPoints;
		for (int j = 0; j < numberOfPoints; j++) {
			double i = j * delta;
			double y = (30 * i - Math.pow(i, 2)) * Math.cos((2 * Math.PI) * i) - 30;
			points.add(new PointImpl(j, y));
		}
		return points;
	}

	public class Tuple<U, V> {
		private final U first;
		private final V second;

		public Tuple(U first, V second) {
			this.first = first;
			this.second = second;
		}

		public V getSecond() {
			return second;
		}

		public U getFirst() {
			return first;
		}
	}

	public class Quint<T, U, V, Y> {

		private final T first;
		private final U second;
		private final V third;
		private final Y fourth;

		public Quint(T first, U second, V third, Y fourth) {
			this.first = first;
			this.second = second;
			this.third = third;
			this.fourth = fourth;
		}

		public T getFirst() {
			return first;
		}

		public U getSecond() {
			return second;
		}

		public V getThird() {
			return third;
		}

		public Y getFourth() {
			return fourth;
		}
	}

	public class Triplet<T, U, V> {

		private final T first;
		private final U second;
		private final V third;

		public Triplet(T first, U second, V third) {
			this.first = first;
			this.second = second;
			this.third = third;
		}

		public T getFirst() {
			return first;
		}

		public U getSecond() {
			return second;
		}

		public V getThird() {
			return third;
		}
	}

	List<Integer> splitSet = new ArrayList<Integer>(Arrays.asList(25000, 75000, 125000 ));
	
	List<Tuple<Integer, Integer>> nodeConsumers = new ArrayList<Tuple<Integer,Integer>>(Arrays.asList(
			new Tuple<Integer,Integer>(1,0),
			new Tuple<Integer,Integer>(1,1),
			new Tuple<Integer,Integer>(2,0),
			new Tuple<Integer,Integer>(2,1),
			new Tuple<Integer,Integer>(2,2),
			new Tuple<Integer,Integer>(3,2)
			));
	
	
	public List<List<NodeConfig>> GetNodeSettings(int nodes){
//		set 1, node1 , node2
		List<List<NodeConfig>> settingsSet = new ArrayList<List<NodeConfig>>(); 
		for( Integer splitValue: splitSet){
			for(Tuple<Integer,Integer> consumers :  nodeConsumers){
				
//				settingsSet.add(test);
			}
		}
		return settingsSet;
	};  
}
