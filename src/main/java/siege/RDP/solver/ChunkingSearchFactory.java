package siege.RDP.solver;

import java.util.concurrent.ExecutorService;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

public class ChunkingSearchFactory {

	private ExecutorService executor;
	private Logger log = Logger.getLogger(this.getClass());


	@Inject
	public ChunkingSearchFactory( ExecutorService executor) {
		this.executor = executor;
	}

	public ISearchStrategy createSearcher(int chunk_size, int max_chunks) {
		return new ChunkingSearch(max_chunks, chunk_size, executor);
	}



	
}
