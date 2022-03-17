package masecla.reddit4j.http.clients;

public class RedditResponse {
    private int statusCode;
    private String responseBody;

    public RedditResponse(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return responseBody;
    }
}
