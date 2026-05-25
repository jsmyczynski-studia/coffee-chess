package pl.coffeechess.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ServiceDiscoveryIntegrationTest {

    private static final String USERNAME = "eureka";
    private static final String PASSWORD = "testSecret";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void microservicesCanRegisterAndBeDiscoveredByName() {
        registerService("USER-SERVICE", "user-service-1", 8081);
        registerService("GAME-SERVICE", "game-service-1", 8082);

        await().untilAsserted(() -> {
            Map<String, Object> registry = fetchRegistry();
            assertThat((Integer) registry.get("registeredServiceCount")).isGreaterThanOrEqualTo(2);
        });

        assertThat(fetchServiceInstances("USER-SERVICE")).hasSize(1);
        assertThat(fetchServiceInstances("GAME-SERVICE")).hasSize(1);

        Map<String, Object> userInstance = fetchServiceInstances("USER-SERVICE").getFirst();
        assertThat(userInstance.get("hostName")).isEqualTo("localhost");
        assertThat(extractPort(userInstance)).isEqualTo(8081);
        assertThat(userInstance.get("status")).isEqualTo("UP");
    }

    @Test
    void registryListsAllRegisteredInstances() {
        registerService("ANALYSIS-SERVICE", "analysis-service-1", 8083);

        await().untilAsserted(() -> {
            Map<String, Object> registry = fetchRegistry();
            assertThat((List<?>) registry.get("services")).isNotEmpty();
        });

        Map<String, Object> registry = fetchRegistry();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> services = (List<Map<String, Object>>) registry.get("services");

        assertThat(services)
                .extracting(service -> service.get("name"))
                .contains("ANALYSIS-SERVICE");
    }

    private void registerService(String appName, String instanceId, int port) {
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
                """.formatted(instanceId, appName, port, port, port, port, appName.toLowerCase(), appName.toLowerCase());

        HttpHeaders headers = basicAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/eureka/apps/" + appName,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.NO_CONTENT, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchServiceInstances(String appName) {
        HttpHeaders headers = basicAuthHeaders();
        ResponseEntity<Map> response = restTemplate.exchange(
                "/eureka/apps/" + appName,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> application = (Map<String, Object>) response.getBody().get("application");
        Object instanceNode = application.get("instance");

        if (instanceNode instanceof List<?> instances) {
            return (List<Map<String, Object>>) instances;
        }

        return List.of((Map<String, Object>) instanceNode);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchRegistry() {
        HttpHeaders headers = basicAuthHeaders();
        ResponseEntity<Map> response = restTemplate.exchange(
                "/actuator/eureka-registry",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private HttpHeaders basicAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String token = Base64.getEncoder().encodeToString(
                (USERNAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8)
        );
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + token);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));
        return headers;
    }

    @SuppressWarnings("unchecked")
    private int extractPort(Map<String, Object> instance) {
        Object portNode = instance.get("port");
        if (portNode instanceof Number number) {
            return number.intValue();
        }
        if (portNode instanceof Map<?, ?> portMap) {
            Object value = portMap.get("$");
            if (value instanceof Number number) {
                return number.intValue();
            }
        }
        throw new IllegalStateException("Unexpected port format: " + portNode);
    }
}
