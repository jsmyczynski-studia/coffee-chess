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
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ServiceDiscoveryIntegrationTest {

    private static final String USERNAME = "eureka";
    private static final String PASSWORD = "testSecret";

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void microservicesCanRegisterAndBeDiscoveredByName() throws Exception {
        registerService("USER-SERVICE", "user-service-1", 8081);
        registerService("GAME-SERVICE", "game-service-1", 8082);

        await().untilAsserted(() -> {
            String registry = fetchRegistry();
            assertThat(registry).contains("USER-SERVICE").contains("GAME-SERVICE");
        });

        String userService = fetchService("USER-SERVICE");
        assertThat(userService).contains("user-service-1");
        assertThat(userService).contains("\"status\":\"UP\"");
        assertThat(userService).contains("\"$\":8081");
    }

    @Test
    void registryListsAllRegisteredInstances() throws Exception {
        registerService("ANALYSIS-SERVICE", "analysis-service-1", 8083);

        await().untilAsserted(() ->
                assertThat(fetchRegistry()).contains("ANALYSIS-SERVICE")
        );
    }

    private void registerService(String appName, String instanceId, int servicePort) throws Exception {
        String payload = """
                {
                  "instance": {
                    "instanceId": "%s",
                    "hostName": "localhost",
                    "app": "%s",
                    "ipAddr": "127.0.0.1",
                    "status": "UP",
                    "overriddenstatus": "UNKNOWN",
                    "port": {"$": %d, "@enabled": "true"},
                    "securePort": {"$": 443, "@enabled": "false"},
                    "countryId": 1,
                    "dataCenterInfo": {
                      "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                      "name": "MyOwn"
                    },
                    "leaseInfo": {
                      "renewalIntervalInSecs": 10,
                      "durationInSecs": 30,
                      "registrationTimestamp": 0,
                      "lastRenewalTimestamp": 0,
                      "evictionTimestamp": 0,
                      "serviceUpTimestamp": 0
                    },
                    "homePageUrl": "http://localhost:%d/",
                    "statusPageUrl": "http://localhost:%d/actuator/info",
                    "healthCheckUrl": "http://localhost:%d/actuator/health",
                    "vipAddress": "%s",
                    "secureVipAddress": "%s",
                    "isCoordinatingDiscoveryServer": "false",
                    "lastUpdatedTimestamp": "0",
                    "lastDirtyTimestamp": "0",
                    "actionType": "ADDED"
                  }
                }
                """.formatted(instanceId, appName, servicePort, servicePort, servicePort, servicePort, appName.toLowerCase(), appName.toLowerCase());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/eureka/apps/" + appName))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .header("Authorization", basicAuth())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isIn(200, 204);
    }

    private String fetchService(String appName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/eureka/apps/" + appName))
                .GET()
                .header("Authorization", basicAuth())
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return response.body();
    }

    private String fetchRegistry() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/actuator/eureka-registry"))
                .GET()
                .header("Authorization", basicAuth())
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return response.body();
    }

    private String basicAuth() {
        String token = Base64.getEncoder().encodeToString(
                (USERNAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8)
        );
        return "Basic " + token;
    }
}
