package masecla.reddit4j.http;

import java.io.IOException;

import masecla.reddit4j.http.clients.RedditRequest;
import masecla.reddit4j.http.clients.RedditResponse;

/**
 * This is a simple template for HttpClients to do requests!
 * 
 * @author Matt
 */
public abstract class GenericHttpClient {
	/**
	 * This will generally call {@link Connection} and return a {@link RedditResponse}. If
	 * any other headers are checked or if the Response is altered is implementation
	 * dependent. This method should be considered and implemented thread-safe.
	 * 
	 * @param connection - The connection with all data needed filled in
	 * @return - A response containing the requested content
	 * @throws IOException          - If the request fails for some reason
	 * @throws InterruptedException - If the client is interrupted while doing the
	 *                              request
	 */
	public abstract RedditResponse execute(RedditRequest connection) throws IOException, InterruptedException;
}
