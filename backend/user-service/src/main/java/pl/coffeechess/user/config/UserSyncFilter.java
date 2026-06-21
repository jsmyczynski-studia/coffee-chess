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
import pl.coffeechess.user.model.entity.User;
import pl.coffeechess.user.repository.UserRepository;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSyncFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            UUID userId = UUID.fromString(jwt.getSubject());

            if (!userRepository.existsById(userId)) {
                String nickname = jwt.getClaimAsString("preferred_username");
                String email = jwt.getClaimAsString("email");

                if (nickname == null) {
                    nickname = "user_" + userId.toString().substring(0, 8);
                }
                if (email == null) {
                    email = nickname + "@example.com";
                }

                User newUser = User.builder()
                        .id(userId)
                        .nickname(nickname)
                        .email(email)
                        .build();

                log.info("Auto-provisioning missing user from JWT: {} ({})", nickname, userId);
                userRepository.save(newUser);
            }
        }

        filterChain.doFilter(request, response);
    }
}
