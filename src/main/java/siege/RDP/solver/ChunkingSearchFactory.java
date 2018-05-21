package siege.RDP.solver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.log4j.Logger;

import siege.RDP.domain.Line;
import siege.RDP.messages.IdentifiableMessage;

public class ChunkingSearchFactory {

	private ExecutorService executor;
	private Logger log = Logger.getLogger(this.getClass());


	public ChunkingSearchFactory( ExecutorService executor) {
		this.executor = executor;
	}

	
	public ISearchStrategy createSearcher(int chunk_size, int max_chunks) {
		return new ChunkingSearch(max_chunks, chunk_size, executor);
	}
	
	
}
