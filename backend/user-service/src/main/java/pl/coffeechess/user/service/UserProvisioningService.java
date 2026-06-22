package pl.coffeechess.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.user.keycloak.KeycloakAdminClient;
import pl.coffeechess.user.model.entity.User;
import pl.coffeechess.user.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Single source of truth for creating local user rows from Keycloak. Both the request-time
 * {@link pl.coffeechess.user.config.UserSyncFilter} (provisions the caller) and the
 * {@link FriendService} (provisions a referenced-but-unseen user) go through here so the
 * local users table stays in sync with Keycloak regardless of who has called the service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserRepository userRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    /**
     * Ensure a local user row exists for the given Keycloak subject, using claims already
     * present in the JWT. Idempotent and safe under concurrent requests.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User ensureUser(UUID userId, String nicknameClaim, String emailClaim) {
        Optional<User> existing = userRepository.findById(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String nickname = nicknameClaim != null
                ? nicknameClaim
                : "user_" + userId.toString().substring(0, 8);
        String email = emailClaim != null
                ? emailClaim
                : nickname + "@example.com";

        return saveProvisioned(userId, nickname, email);
    }

    /**
     * Resolve a user by nickname. If the user is not yet in the local table but exists in
     * Keycloak, provision them on the fly. This is what fixes friend invites for users who
     * have logged into Keycloak but never called user-service.
     */
    @Transactional
    public Optional<User> resolveByNickname(String nickname) {
        Optional<User> local = userRepository.findByNickname(nickname);
        if (local.isPresent()) {
            return local;
        }

        return keycloakAdminClient.findByUsername(nickname).map(kcUser -> {
            UUID id = UUID.fromString(kcUser.id());
            String email = kcUser.email() != null ? kcUser.email() : nickname + "@example.com";
            log.info("Provisioning user '{}' ({}) from Keycloak on friend-request lookup", nickname, id);
            return saveProvisioned(id, kcUser.username(), email);
        });
    }

    /**
     * Resolve a Keycloak subject to an application profile. This covers asynchronous
     * flows (for example a Kafka game-completed event) that do not carry the user's JWT.
     */
    @Transactional
    public Optional<User> resolveById(UUID userId) {
        Optional<User> local = userRepository.findById(userId);
        if (local.isPresent()) {
            return local;
        }

        return keycloakAdminClient.findById(userId.toString()).map(kcUser -> {
            String nickname = kcUser.username() != null
                    ? kcUser.username()
                    : "user_" + userId.toString().substring(0, 8);
            String email = kcUser.email() != null
                    ? kcUser.email()
                    : nickname + "@example.com";
            log.info("Provisioning user '{}' ({}) from Keycloak by subject", nickname, userId);
            return saveProvisioned(userId, nickname, email);
        });
    }

    private User saveProvisioned(UUID userId, String nickname, String email) {
        try {
            User newUser = User.builder()
                    .id(userId)
                    .nickname(nickname)
                    .email(email)
                    .build();
            User saved = userRepository.saveAndFlush(newUser);
            log.info("Provisioned local user: {} ({})", nickname, userId);
            return saved;
        } catch (DataIntegrityViolationException e) {
            // Lost a race with a concurrent provision of the same id; just read it back.
            return userRepository.findById(userId)
                    .orElseThrow(() -> e);
        }
    }
}
