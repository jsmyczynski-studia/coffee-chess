package pl.coffeechess.eureka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("prod")
public class EurekaProductionGuard {

    private static final Set<String> FORBIDDEN_PASSWORDS = Set.of(
            "eurekaSecret123",
            "secret",
            "password",
            "changeme"
    );

    public EurekaProductionGuard(@Value("${eureka.security.password}") String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("EUREKA_PASSWORD must be set in production");
        }
        if (password.length() < 16) {
            throw new IllegalStateException("EUREKA_PASSWORD must be at least 16 characters in production");
        }
        if (FORBIDDEN_PASSWORDS.contains(password)) {
            throw new IllegalStateException("EUREKA_PASSWORD uses a forbidden default value in production");
        }
    }
}
