package masecla.reddit4j.http.clients;

import java.io.IOException;

public interface RedditHttpClient {
    RedditResponse execute(RedditRequest request) throws IOException;
}
