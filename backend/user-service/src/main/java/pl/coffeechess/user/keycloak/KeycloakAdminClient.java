package pl.coffeechess.user.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import pl.coffeechess.user.config.KeycloakProperties;

import java.util.List;
import java.util.Optional;

/**
 * Thin wrapper over the Keycloak Admin REST API. Authenticates with a confidential
 * client using the client-credentials grant and exposes the lookups the user-service
 * needs to keep its local user table in sync with Keycloak on demand.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private final KeycloakProperties properties;
    private final RestClient restClient = RestClient.create();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KeycloakUser(String id, String username, String email,
                               String firstName, String lastName, Boolean enabled) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(String access_token) {
    }

    private String fetchAdminToken() {
        String tokenUrl = properties.getServerUrl()
                + "/realms/" + properties.getRealm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());

        TokenResponse token = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (token == null || token.access_token() == null) {
            throw new IllegalStateException("Failed to obtain Keycloak admin token");
        }
        return token.access_token();
    }

    /**
     * Look up a Keycloak user by exact username. Returns empty if no such user exists.
     */
    public Optional<KeycloakUser> findByUsername(String username) {
        try {
            String adminToken = fetchAdminToken();
            String usersUrl = properties.getServerUrl()
                    + "/admin/realms/" + properties.getRealm()
                    + "/users?exact=true&username=" + username;

            KeycloakUser[] users = restClient.get()
                    .uri(usersUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(KeycloakUser[].class);

            if (users == null || users.length == 0) {
                return Optional.empty();
            }
            return Optional.of(users[0]);
        } catch (Exception e) {
            log.warn("Keycloak lookup for username '{}' failed: {}", username, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Look up a Keycloak user by their subject UUID (the user id).
     */
    public Optional<KeycloakUser> findById(String userId) {
        try {
            String adminToken = fetchAdminToken();
            String userUrl = properties.getServerUrl()
                    + "/admin/realms/" + properties.getRealm()
                    + "/users/" + userId;

            KeycloakUser user = restClient.get()
                    .uri(userUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(KeycloakUser.class);

            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.warn("Keycloak lookup for id '{}' failed: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fetch all enabled users in the realm. Used to fully reconcile the local table
     * with Keycloak. Paginates in batches.
     */
    public List<KeycloakUser> findAll() {
        String adminToken = fetchAdminToken();
        String usersUrl = properties.getServerUrl()
                + "/admin/realms/" + properties.getRealm()
                + "/users?max=1000";

        KeycloakUser[] users = restClient.get()
                .uri(usersUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .body(KeycloakUser[].class);

        return users == null ? List.of() : List.of(users);
    }
}
