package masecla.reddit4j.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import masecla.reddit4j.http.Method;
import masecla.reddit4j.http.clients.RedditHttpClient;
import masecla.reddit4j.http.clients.RedditRequest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.http.GenericHttpClient;
import masecla.reddit4j.http.clients.RateLimitedClient;
import masecla.reddit4j.http.clients.RedditResponse;
import masecla.reddit4j.objects.*;
import masecla.reddit4j.objects.preferences.RedditPreferences;
import masecla.reddit4j.objects.subreddit.RedditSubreddit;
import masecla.reddit4j.requests.ListingEndpointRequest;
import masecla.reddit4j.requests.RedditPreferencesUpdateRequest;

public class Reddit4J {

    private static String BASE_URL = "https://www.reddit.com";
    private static String OAUTH_URL = "https://oauth.reddit.com";

    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String userAgent;

    private String token;
    private long expirationDate = -1;
    private GenericHttpClient httpClient;

    private ClientType clientType;

    protected Reddit4J() {

    }

    public void userlessConnect() throws IOException, InterruptedException, AuthenticationException {
        if (userAgent == null) {
            throw new NullPointerException("User Agent was not set!");
        }
        RedditRequest conn = RedditRequest.connect(BASE_URL + "/api/v1/access_token").ignoreContentType(true)
                .ignoreHttpErrors(true).method(Method.POST).userAgent(userAgent);
        conn.data("grant_type", "client_credentials");

        // Generate the Authorization header
        String combination = clientId + ":" + clientSecret;
        combination = Base64.getEncoder().encodeToString(combination.getBytes());
        conn.header("Authorization", "Basic " + combination);

        RedditResponse response = httpClient.execute(conn);
        if (response.statusCode() == 401) {
            throw new AuthenticationException("Unauthorized! Invalid clientId or clientSecret!");
        }

        JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();

        // Something went wrong
        if (object.keySet().contains("error")) {
            throw new AuthenticationException(object.get("error").getAsString());
        }
        this.token = object.get("access_token").getAsString();
        this.expirationDate = object.get("expires_in").getAsInt() + Instant.now().getEpochSecond();
        this.clientType = ClientType.APPLICATION_ONLY;
    }

    public void connect() throws IOException, InterruptedException, AuthenticationException {
        if (userAgent == null) {
            throw new NullPointerException("User Agent was not set!");
        }
        RedditRequest conn = RedditRequest.connect(BASE_URL + "/api/v1/access_token").ignoreContentType(true)
                .ignoreHttpErrors(true).method(Method.POST).userAgent(userAgent);
        // Set the required params;
        conn.data("grant_type", "password");
        conn.data("username", username).data("password", password);

        // Generate the Authorization header
        String combination = clientId + ":" + clientSecret;
        combination = Base64.getEncoder().encodeToString(combination.getBytes());
        conn.header("Authorization", "Basic " + combination);

        RedditResponse response = httpClient.execute(conn);
        if (response.statusCode() == 401) {
            throw new AuthenticationException("Unauthorized! Invalid clientId or clientSecret!");
        }

        JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();

        // Something went wrong
        if (object.keySet().contains("error")) {
            throw new AuthenticationException(object.get("error").getAsString());
        }
        this.token = object.get("access_token").getAsString();
        this.expirationDate = object.get("expires_in").getAsInt() + Instant.now().getEpochSecond();
        this.clientType = ClientType.PERSONAL_SCRIPT;
    }

    public RedditRequest useEndpoint(String endpointPath) {
        RedditRequest connection = RedditRequest.connect(OAUTH_URL + endpointPath);
        connection.header("Authorization", "bearer " + token).ignoreContentType(true).userAgent(userAgent);
        return connection;
    }

    public void ensureConnection() throws IOException, InterruptedException, AuthenticationException {
        // There is no token
        if (token == null) {
            connect();
            return;
        }
        // The token is expired
        if (Instant.now().getEpochSecond() > expirationDate) {
            connect();
            return;
        }
    }

    public List<KarmaBreakdown> getKarmaBreakdown() throws IOException, InterruptedException {
        RedditRequest conn = useEndpoint("/api/v1/me/karma").method(Method.GET);
        RedditResponse rsp = this.httpClient.execute(conn);
        List<KarmaBreakdown> result = new ArrayList<>();
        Gson gson = new Gson();
        JsonParser.parseString(rsp.body()).getAsJsonObject().get("data").getAsJsonArray()
                .forEach(c -> result.add(gson.fromJson(c, KarmaBreakdown.class)));
        return result;
    }

    public RedditPreferences getPreferences() throws IOException, InterruptedException {
        RedditRequest conn = useEndpoint("/api/v1/me/prefs").method(Method.GET);
        RedditResponse rsp = this.httpClient.execute(conn);
        Gson gson = new RedditPreferences().getGson();
        RedditPreferences prf = gson.fromJson(rsp.body(), RedditPreferences.class);
        prf.setClient(this);
        return prf;
    }

    public Reddit4JBeta beta() {
        return new Reddit4JBeta(this);
    }

    public RedditPreferencesUpdateRequest updatePreferences() {
        return new RedditPreferencesUpdateRequest(this);
    }

    public ListingEndpointRequest<RedditUser> getBlocked() {
        return new ListingEndpointRequest<>("/prefs/blocked", this, RedditUser.class);
    }

    public ListingEndpointRequest<RedditUser> getMessaging() {
        return new ListingEndpointRequest<RedditUser>("/prefs/messaging", this, RedditUser.class) {
            @Override
            public String preprocess(String body) {
                JsonArray array = JsonParser.parseString(body).getAsJsonArray();
                return array.get(0).getAsJsonObject().toString();
            }
        };
    }

    public ListingEndpointRequest<RedditUser> getTrusted() {
        return new ListingEndpointRequest<>("/prefs/trusted", this, RedditUser.class);
    }

    public RedditSubreddit getSubreddit(String name) throws IOException, InterruptedException {
        if (name.startsWith("r/"))
            name = name.substring(2);
        if (name.startsWith("/"))
            name = name.substring(1);
        if (name.endsWith("/"))
            name = name.substring(0, name.length() - 1);

        RedditRequest conn = useEndpoint("/r/" + name + "/about");
        RedditResponse rsp = this.httpClient.execute(conn);
        Gson gson = new RedditSubreddit().getGson();
        JsonObject data = JsonParser.parseString(rsp.body()).getAsJsonObject().getAsJsonObject("data");
        RedditSubreddit result = gson.fromJson(data, RedditSubreddit.class);
        result.setClient(this);
        return result;
    }

    public ListingEndpointRequest<RedditUser> getFriends() {
        return new ListingEndpointRequest<RedditUser>("/prefs/friends", this, RedditUser.class) {
            @Override
            public String preprocess(String body) {
                JsonArray array = JsonParser.parseString(body).getAsJsonArray();
                return array.get(0).getAsJsonObject().toString();
            }
        };
    }

    public List<RedditTrophy> getTrophies() throws IOException, InterruptedException {
        RedditRequest conn = useEndpoint("/api/v1/me/trophies").method(Method.GET);
        RedditResponse rsp = this.httpClient.execute(conn);
        List<RedditTrophy> trophies = new ArrayList<>();
        Gson gson = new RedditTrophy().getGson();
        JsonArray trophyArray = JsonParser.parseString(rsp.body()).getAsJsonObject().getAsJsonObject("data")
                .getAsJsonArray("trophies");
        trophyArray.forEach(c -> {
            c = c.getAsJsonObject().getAsJsonObject("data");
            trophies.add(gson.fromJson(c, RedditTrophy.class));
        });
        return trophies;
    }

    public RedditProfile getSelfProfile() throws IOException, InterruptedException, AuthenticationException {
        ensureConnection();
        RedditRequest request = useEndpoint("/api/v1/me");
        RedditResponse rsp = httpClient.execute(request);
        RedditProfile properties = new Gson().fromJson(rsp.body(), RedditProfile.class);
        return properties;
    }

    public static Reddit4J rateLimited(RedditHttpClient httpClient) {
        Reddit4J result = new Reddit4J();
        result.httpClient = new RateLimitedClient(httpClient);
        return result;
    }

    @Deprecated
    public static Reddit4J unlimited() {
        Reddit4J result = new Reddit4J();
        result.httpClient = new masecla.reddit4j.http.clients.UnlimitedClient(null);
        return result;
    }

    public Reddit4J setUsername(String username) {
        this.username = username;
        return this;
    }

    public Reddit4J setPassword(String password) {
        this.password = password;
        return this;
    }

    public Reddit4J setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public Reddit4J setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public Reddit4J setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public Reddit4J setUserAgent(UserAgentBuilder userAgent) {
        this.userAgent = userAgent.toString();
        return this;
    }

    public Reddit4J applyJdk8HttpFix() {
        allowMethods("PATCH");
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getToken() {
        return token;
    }

    public long getTokenExpirationDate() {
        return expirationDate;
    }

    public GenericHttpClient getHttpClient() {
        return httpClient;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public static String BASE_URL() {
        return BASE_URL;
    }

    public static String OAUTH_URL() {
        return OAUTH_URL;
    }

    private static boolean initialized = false;

    /**
     * This will force the {@link HttpURLConnection} to accept methods which would
     * otherwise not be allowed, such as PATCH. See
     * https://bugs.openjdk.java.net/browse/JDK-7016595, and
     * https://stackoverflow.com/questions/25163131/httpurlconnection-invalid-http-method-patch
     * 
     * @param methods - The methods to force {@link HttpURLConnection} to take.
     */
    private static void allowMethods(String... methods) {
        if (initialized)
            return;
        initialized = true;

        try {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

            methodsField.setAccessible(true);

            String[] oldMethods = (String[]) methodsField.get(null);
            Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
            methodsSet.addAll(Arrays.asList(methods));
            String[] newMethods = methodsSet.toArray(new String[0]);

            methodsField.set(null, newMethods);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * @return - The client type of this application
     */
    public ClientType getClientType() {
        return clientType;
    }

}
