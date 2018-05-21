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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class DistributedTest {

	@Test
	public void SimpleRun() {
		try {

			Client client = Client.getClient();
			IRDPService service = client.man.getRDPService();

			List<PointImpl> n = create(1000000);
			NodeConfig settings = new NodeConfig();
			settings.consumers = 2;
			settings.search_chunk_size = 25000;
			settings.max_partitions = 2;
			
			IUpdatableNode node1 = client.man.getUpdatableNode("192.168.1.11");
			// IUpdatableNode node2 = ProcessingNode.connect("192.168.1.250");
			// IUpdatableNode node3 = ProcessingNode.connect("192.168.1.250");

			node1.update(settings);
			// node2.update(settings);
			// node2.update(settings);

			Stopwatch w = createUnstarted();
			w.start();
			n = service.submit(n, 0.001);

			System.out.printf("%d -> %d\n", n.size(), w.stop().elapsed(TimeUnit.NANOSECONDS));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void MeasureRun() {
		// initialize nodes

		Client client = Client.getClient();
		IRDPService service = client.man.getRDPService();

		NodeConfig nodesettings = new NodeConfig();
		IUpdatableNode node1 = client.man.getUpdatableNode("192.168.1.250");
		IUpdatableNode node2 = client.man.getUpdatableNode("192.168.1.13");
		// IUpdatableNode node3 = ProcessingNode.connect("192.168.1.250");
		try {
			CSVPrinter csvPrinter = createCsvFile();
			List<Integer[]> settings = GetNodeSettings();
			for (Integer[] setting : settings) {
				

				nodesettings.consumers = setting[2];
				nodesettings.search_chunk_size = setting[5];
				nodesettings.cores = 4;
				nodesettings.useAnnounce = true;
				nodesettings.max_partitions = setting[0];
				node1.update(nodesettings);

				 nodesettings.consumers = setting[3];
				 nodesettings.cores = 4;
				 nodesettings.useAnnounce = true;
				 nodesettings.search_chunk_size = setting[5];
				 nodesettings.max_partitions = setting[0];
				 node2.update(nodesettings);
				//
				// nodesettings.consumers = setting[4];
				// node3.update(nodesettings);


				for (int i = 0; i <= 5; i++) {
					List<PointImpl> n = create(setting[1]);
					Stopwatch w = createStarted();
					n = service.submit(n, 0.001);
					csvPrinter.printRecord(w.stop().elapsed(TimeUnit.NANOSECONDS), setting[1], setting[0], setting[2], setting[3], setting[5]);
					csvPrinter.flush();
					Thread.sleep(500);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	List<Integer> partitionSet = new ArrayList<Integer>(Arrays.asList(3,4));
	List<Integer> sizeSet = new ArrayList<Integer>(Arrays.asList(4000000,3000000,1000000 ));

	List<Integer> search_chunksizeSet = new ArrayList<Integer>(Arrays.asList(40000));

	List<Triplet<Integer, Integer, Integer>> nodeConsumers = new ArrayList<Triplet<Integer, Integer, Integer>>(
			Arrays.asList(
					new Triplet<Integer, Integer, Integer>(2, 2, 1),
					new Triplet<Integer, Integer, Integer>(1, 1, 1),
					new Triplet<Integer, Integer, Integer>(1, 2, 1),
					new Triplet<Integer, Integer, Integer>(2, 1, 1)
//					new Triplet<Integer, Integer, Integer>(0, 1, 1),
//					new Triplet<Integer, Integer, Integer>(1, 0, 1)
//					new Triplet<Integer, Integer, Integer>(1, 1, 1),
//					new Triplet<Integer, Integer, Integer>(2, 0, 1), 
//					new Triplet<Integer, Integer, Integer>(0, 2, 1), 
//					new Triplet<Integer, Integer, Integer>(1, 2, 1),
//					new Triplet<Integer, Integer, Integer>(2, 1, 1),
//					new Triplet<Integer, Integer, Integer>(2, 2, 1),
//					new Triplet<Integer, Integer, Integer>(2, 2, 1)
					));

	public List<Integer[]> GetNodeSettings() {
		// split, size, node1consumers, node2consumers, node3consumers
		ArrayList<Integer[]> settings = new ArrayList<>();
		for (Integer split : partitionSet) {
			for (Integer size : sizeSet) {
				for(Integer chunk : search_chunksizeSet){
					for (Triplet<Integer, Integer, Integer> nodes : nodeConsumers) {
						settings.add(new Integer[] { split, size, nodes.first, nodes.second, nodes.third, chunk});
					}
				}
			}
		}
		return settings;
	};

	
	private CSVPrinter createCsvFile() {
		try {
			String date = new SimpleDateFormat("yyyy-MM-dd-HHmm").format(new Date());
			Path p = Paths.get(String.format("./results/%s.%s", date, "csv"));
			new File(p.toString()).createNewFile();
			System.out.println(p.toAbsolutePath().toString());
			BufferedWriter writer = Files.newBufferedWriter(p);
			return new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("time_ns",  "n", "partition_ancestors", "node1_consumers", "node2_consumers", "chunks")); 			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
		Random r = new Random();
		List<PointImpl> points = new ArrayList<PointImpl>();
		double delta = 30 / (double) numberOfPoints;
		for (int j = 0; j < numberOfPoints; j++) {
			float rf = r.nextFloat();
			double i = j * delta;
			double y = ((60 * rf) * i - Math.pow(i, 2)) * Math.cos((2 * Math.PI) * i) - (60 * rf);
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
	
	

}
