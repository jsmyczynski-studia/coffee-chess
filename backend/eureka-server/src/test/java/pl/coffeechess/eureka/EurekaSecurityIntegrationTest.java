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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EurekaSecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpointIsPublic() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("status");
    }

    @Test
    void eurekaDashboardRequiresAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void eurekaRegistryEndpointRequiresAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/eureka-registry", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticatedClientCanAccessRegistryEndpoint() {
        HttpHeaders headers = basicAuthHeaders("eureka", "testSecret");
        ResponseEntity<Map> response = restTemplate.exchange(
                "/actuator/eureka-registry",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKeys("registeredServiceCount", "registeredInstanceCount", "services");
    }

    private HttpHeaders basicAuthHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        String token = Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes(StandardCharsets.UTF_8)
        );
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + token);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));
        return headers;
    }
}
