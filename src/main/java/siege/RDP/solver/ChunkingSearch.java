package siege.RDP.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import siege.RDP.domain.Line;

public class ChunkingSearch implements ISearchStrategy {

	private Logger log = Logger.getLogger(this.getClass());
	private ExecutorService executor;
	
	private int chunks_max;
	private int chunk_size;
	
	public ChunkingSearch(int chunks_max, int chunk_size, ExecutorService executor) {
		this.chunk_size = chunk_size;
		this.chunks_max = chunks_max;
		this.executor = executor;
	}
	
	@Override
	public SearchResult findMaximum(Line line) {
		try {
			int parts = (int) Math.ceil(line.getPoints().size() / chunk_size) + 1;
			parts = parts > chunks_max ? chunks_max : parts;

			if (parts > 1) {

				int chunkSize = (int) Math.ceil(line.getPoints().size() / parts);
				List<SearchJob> searchTasks = new ArrayList<>(parts);

				for (int j = 0; j < parts; j++) {
					int start = j * chunkSize;
					int end = (j + 1) * chunkSize;
					SearchJob segment = new SearchJob(line, start, end);
					searchTasks.add(segment);
				}

				List<Future<SearchResult>> searchResults = executor
						.invokeAll(searchTasks.subList(1, searchTasks.size() - 1));

				SearchResult topResult = searchTasks.get(0).call();
				for (Future<SearchResult> future : searchResults) {
					SearchResult contendr = future.get();
					if (contendr.compareTo(topResult) >= 0) {
						topResult = contendr;
					}
				}
				return topResult;

			} else {
				return new SearchJob(line, 0, line.getPoints().size() - 1).call();
			}
		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
		}
		return null;

	}
	
}
