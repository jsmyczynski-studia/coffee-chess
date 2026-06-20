package pl.coffeechess.eureka.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "eureka.security.username=eureka",
        "eureka.security.password=ThisIsAVeryStrongProductionPassword123!"
})
class EurekaProductionGuardTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void startsWhenProductionPasswordIsStrong() {
        assertThat(applicationContext.getBean(EurekaProductionGuard.class)).isNotNull();
    }

    @Test
    void rejectsDefaultPasswordInProduction() {
        assertThrows(IllegalStateException.class, () ->
                new EurekaProductionGuard("eurekaSecret123")
        );
    }

    @Test
    void rejectsShortPasswordInProduction() {
        assertThrows(IllegalStateException.class, () ->
                new EurekaProductionGuard("short")
        );
    }
}
