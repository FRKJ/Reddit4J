package masecla.reddit4j.client;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.jsoup.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest
class Reddit4JTest {
    private WireMockRuntimeInfo wmRuntimeInfo;
    private Reddit4J reddit4J;

    private final String TEST_USER_AGENT = "TEST_USER_AGENT";

    public Reddit4JTest(WireMockRuntimeInfo wmRuntimeInfo) {
        this.wmRuntimeInfo = wmRuntimeInfo;
    }

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        int port = wmRuntimeInfo.getHttpPort();
        Reddit4J.setBaseUrl(String.format("http://localhost:%d", port));
        Reddit4J.setOauthUrl(String.format("http://localhost:%d", port));

        reddit4J = Reddit4J.unlimited().setUserAgent(TEST_USER_AGENT);

        stubFor(post("/api/v1/access_token").willReturn(ok().withBodyFile("test.json")));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void userlessConnect() throws Exception {
        reddit4J.userlessConnect();
    }

    @Test
    void connect() throws Exception {
        reddit4J.connect();
    }

    @Test
    void useEndpoint() {
        Connection connection = reddit4J.useEndpoint("/test");
    }
}