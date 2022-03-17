package masecla.reddit4j.http.clients;

import masecla.reddit4j.http.Method;

import java.util.HashMap;
import java.util.Map;

public class RedditRequest {
    private String url;
    private boolean ignoreContentType;
    private boolean ignoreHttpErrors;
    private Method method;
    private Map<String, String> bodyMap;
    private Map<String, String> headerMap;
    private String userAgent;

    public RedditRequest(String url) {
        this.url = url;
        this.method = Method.GET;
    }

    public RedditRequest ignoreContentType(boolean ignoreContentType) {
        this.ignoreContentType = ignoreContentType;
        return this;
    }

    public RedditRequest ignoreHttpErrors(boolean ignoreHttpErrors) {
        this.ignoreHttpErrors = ignoreHttpErrors;
        return this;
    }

    public RedditRequest method(Method method) {
        this.method = method;
        return this;
    }

    public RedditRequest data(String key, String value) {
        if (bodyMap == null) {
            bodyMap = new HashMap<>();
        }
        bodyMap.put(key, value);
        return this;
    }

    public RedditRequest userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public RedditRequest header(String key, String value) {
        if (headerMap == null) {
            headerMap = new HashMap<>();
        }
        headerMap.put(key, value);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public boolean isIgnoreContentType() {
        return ignoreContentType;
    }

    public boolean isIgnoreHttpErrors() {
        return ignoreHttpErrors;
    }

    public Method getMethod() {
        return method;
    }

    public Map<String, String> getBodyMap() {
        return bodyMap;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public static RedditRequest connect(final String url)
    {
        return new RedditRequest(url);
    }
}
