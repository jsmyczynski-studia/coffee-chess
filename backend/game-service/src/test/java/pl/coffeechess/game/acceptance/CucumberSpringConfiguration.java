package pl.coffeechess.game.acceptance;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.coffeechess.game.client.EngineClient;
import pl.coffeechess.game.client.LlmClient;
import pl.coffeechess.game.kafka.GameCompletedProducer;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
@Import(CucumberSpringConfiguration.TestSecuritySupport.class)
public class CucumberSpringConfiguration {

    @MockitoBean
    EngineClient engineClient;

    @MockitoBean
    LlmClient llmClient;

    @MockitoBean
    GameCompletedProducer gameCompletedProducer;

    @TestConfiguration
    static class TestSecuritySupport {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .subject("test-user")
                    .build();
        }
    }
}
