package pl.coffeechess.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for talking to the Keycloak Admin REST API. The user-service needs this
 * so it can resolve / provision users that exist in Keycloak but have not yet hit this
 * service themselves (e.g. the addressee of a friend request).
 */
@Component
@ConfigurationProperties(prefix = "keycloak.admin")
@Getter
@Setter
public class KeycloakProperties {

    /** Base URL of the Keycloak server, e.g. http://keycloak:8080 */
    private String serverUrl = "http://localhost:8080";

    /** Realm the application users live in. */
    private String realm = "coffee-chess";

    /** Confidential client used for the admin client-credentials grant. */
    private String clientId = "user-service-admin";

    /** Secret for the confidential client above. */
    private String clientSecret = "user-service-admin-secret";
}
