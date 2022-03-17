package masecla.reddit4j.http.clients;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Optional;

/**
 * Http Client using Apache HttpClient 4
 */
public class HttpClient4 implements RedditHttpClient {
    private final CloseableHttpClient httpClient;

    public HttpClient4(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public RedditResponse execute(RedditRequest request) throws IOException {
        final RequestBuilder requestBuilder;

        // Method
        switch (request.getMethod()) {
            case GET:
                requestBuilder = RequestBuilder.get(request.getUrl());
                break;
            case POST:
                requestBuilder = RequestBuilder.post(request.getUrl());
                break;
            case PATCH:
                requestBuilder = RequestBuilder.patch(request.getUrl());
                break;
            case DELETE:
                requestBuilder = RequestBuilder.delete(request.getUrl());
                break;
            default:
                throw new IllegalStateException("RedditRequest method not supported: " + request.getMethod());
        }

        // Headers
        Optional.ofNullable(request.getHeaderMap()).ifPresent(headerMap -> headerMap.forEach(requestBuilder::addHeader));

        // User agent
        if (!request.getUserAgent().isEmpty()) {
            requestBuilder.setHeader(HttpHeaders.USER_AGENT, request.getUserAgent());
        }

        // Parameters
        Optional.ofNullable(request.getBodyMap()).ifPresent(bodyMap -> bodyMap.forEach(requestBuilder::addParameter));

        try (CloseableHttpResponse httpResponse = httpClient.execute(requestBuilder.build()))  {
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            final String responseBody = EntityUtils.toString(httpResponse.getEntity());

            return new RedditResponse(statusCode, responseBody);
        }
    }
}

