package siege.RDP.messages;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import siege.RDP.domain.Line;

public class RDPSearchContainer extends IdentifiableMessage {
	private List<RDPSearch> searchTasks;
	private Line line;
	private ExecutorService executor;

	public RDPSearchContainer(Line line, int parts, ExecutorService executor) {
		this.line = line;
		this.executor = executor;
		int chunkSize = (int) Math.ceil(line.getPoints().size() / parts);
		searchTasks = new ArrayList<>(parts);
		for (int j = 0; j < parts; j++) {
			int start = j * chunkSize;
			int end = (j + 1) * chunkSize;
			RDPSearch segment = new RDPSearch(line, start, end);
			searchTasks.add(segment);
		}
	}

	public RDPSearch submitAndAwaitResult() {
		// do first one one current thread, submit others to queue
		try {
			List<Future<Void>> completions = executor.invokeAll(searchTasks.subList(1, searchTasks.size() - 1));
			searchTasks.get(0).call();
			for (Future<Void> future : completions) {
				future.get();
			}
			return searchTasks.stream().max(RDPSearchContainer.searchCompare).get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Comparator<RDPSearch> searchCompare = new Comparator<RDPSearch>() {
		public int compare(RDPSearch o1, RDPSearch o2) {
			if(o1.furthestDistance < o2.furthestDistance) return -1;
			if(o1.furthestDistance > o2.furthestDistance) return 1;
			return 0;
		};
	};
	
	@Override
	public String Identifier() {
		return String.format("%d search", searchTasks.size());
	}

}
