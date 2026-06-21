package pl.coffeechess.user.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.coffeechess.user.service.UserProvisioningService;

import java.io.IOException;
import java.util.UUID;

/**
 * Ensures the authenticated caller has a local user row, provisioning it from the JWT
 * claims if needed. Provisioning logic itself lives in {@link UserProvisioningService}
 * so it is shared (and consistent) with on-demand provisioning of referenced users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSyncFilter extends OncePerRequestFilter {

    private final UserProvisioningService provisioningService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            try {
                UUID userId = UUID.fromString(jwt.getSubject());
                provisioningService.ensureUser(
                        userId,
                        jwt.getClaimAsString("preferred_username"),
                        jwt.getClaimAsString("email"));
            } catch (Exception e) {
                log.warn("User sync failed for subject {}: {}", jwt.getSubject(), e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
