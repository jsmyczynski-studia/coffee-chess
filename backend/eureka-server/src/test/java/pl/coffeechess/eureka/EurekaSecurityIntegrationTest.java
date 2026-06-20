package pl.coffeechess.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EurekaSecurityIntegrationTest {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void healthEndpointIsPublic() throws Exception {
        HttpResponse<String> response = sendGet("/actuator/health/liveness", null);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    void eurekaDashboardRequiresAuthentication() throws Exception {
        HttpResponse<String> response = sendGet("/", null);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void eurekaRegistryEndpointRequiresAuthentication() throws Exception {
        HttpResponse<String> response = sendGet("/actuator/eureka-registry", null);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void authenticatedClientCanAccessRegistryEndpoint() throws Exception {
        HttpResponse<String> response = sendGet("/actuator/eureka-registry", basicAuth("eureka", "testSecret"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("registeredServiceCount");
    }

    private HttpResponse<String> sendGet(String path, String authorizationHeader) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .header("Accept", "application/json");

        if (authorizationHeader != null) {
            builder.header("Authorization", authorizationHeader);
        }

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String basicAuth(String username, String password) {
        String token = Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes(StandardCharsets.UTF_8)
        );
        return "Basic " + token;
    }
}
