package masecla.reddit4j.http.clients;

import java.io.IOException;

import masecla.reddit4j.http.GenericHttpClient;

/**
 * This implementation of {@link GenericHttpClient} is completely unlimited and
 * will disregard everything in sight!
 * 
 * @deprecated - To discourage users from using it!
 * @author Matt
 */
@Deprecated
public class UnlimitedClient extends GenericHttpClient {

	private RedditHttpClient httpClient;

	public UnlimitedClient(RedditHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public RedditResponse execute(RedditRequest connection) throws IOException, InterruptedException {
		return httpClient.execute(connection);
	}
}
