package masecla.reddit4j.http.clients;

import masecla.reddit4j.http.Method;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class JsoupHttpClient implements RedditHttpClient {
    @Override
    public RedditResponse execute(RedditRequest request) throws IOException {
        Connection.Response response
                = Jsoup.connect(request.getUrl())
                .ignoreContentType(request.isIgnoreContentType())
                .ignoreHttpErrors(request.isIgnoreHttpErrors())
                .method(convert(request.getMethod()))
                .data(request.getBodyMap())
                .headers(request.getHeaderMap())
                .userAgent(request.getUserAgent())
                .execute();

        return new RedditResponse(response.statusCode(), response.body());
    }

    private Connection.Method convert(Method inputMethod) {
        switch (inputMethod) {
            case GET:
                return Connection.Method.GET;
            case POST:
                return Connection.Method.POST;
            case DELETE:
                return Connection.Method.DELETE;
            case PATCH:
                return Connection.Method.PATCH;
            default:
                throw new IllegalStateException("Unimplemented method " + inputMethod);
        }
    }
}
