package masecla.reddit4j.http.clients;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import masecla.reddit4j.http.GenericHttpClient;

public class RateLimitedClient extends GenericHttpClient {

	/**
	 * This is the maximum amount of requests per minute a client is allowed to do.
	 * This is according to the https://github.com/reddit-archive/reddit/wiki/API
	 */
	private static int RATE_LIMIT_MINUTE = 60;

	private RedditHttpClient httpClient;

	public RateLimitedClient(RedditHttpClient httpClient) {
		super();
		this.httpClient = httpClient;
		this.requestCount = new AtomicInteger(0);
		this.batchStart = new AtomicLong(Instant.now().getEpochSecond());
	}

	@Override
	public RedditResponse execute(RedditRequest request) throws IOException, InterruptedException {
		int secondsToWait = getSecondsUntilNextRequest();
		if (secondsToWait == 0) {
			this.requestCount.incrementAndGet();
			return httpClient.execute(request);
		}
		Thread.sleep(secondsToWait * 1000);
		return execute(request);
	}

	private AtomicInteger requestCount;
	private AtomicLong batchStart;

	private int getSecondsUntilNextRequest() {
		// Check if we are still within the current minute.
		// If not, we can start a new batch
		if (Instant.now().getEpochSecond() - batchStart.get() > 60) {
			requestCount.set(0);
			batchStart.set(Instant.now().getEpochSecond());
			return 0;
		}

		// We still have space to do requests
		if (requestCount.get() < RATE_LIMIT_MINUTE) {
			return 0;
		}

		// The amount of requests this batch is too much, wait until next batch (and one
		// extra second to be sure)
		return 61 - (int) (Instant.now().getEpochSecond() - batchStart.get());
	}

}
