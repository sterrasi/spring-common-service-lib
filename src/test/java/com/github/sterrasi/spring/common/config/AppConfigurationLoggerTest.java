package com.github.sterrasi.spring.common.config;

import ch.qos.logback.classic.Level;
import com.github.sterrasi.spring.common.test.utils.TestAppender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.ActiveProfiles;

import static com.github.sterrasi.spring.common.config.AppConfigurationLogger.BANNER;
import static com.github.sterrasi.spring.common.config.AppConfigurationLogger.CONFIG_LOGGER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The application context is recycled in this test so the {@link AppConfigurationLogger} runs only once.
 */
@SpringBootTest(classes = {AppConfigurationLogger.class})
@Import({AppConfigurationLoggerTest.AppenderInitializationHook.class})
@ActiveProfiles({"app-config-logger"})
class AppConfigurationLoggerTest {

    @Test
    @DisplayName("Ensure that the AppConfigurationLogger gets called")
    void bannerIsLogged() {
        assertThat(TestAppender.contains(BANNER)).isTrue();
    }

    /**
     * The properties used to configure the actual {@link AppConfigurationLogger} should
     * not be logged.
     */
    @Test
    @DisplayName("app logger configuration should not be logged")
    void propsForAppConfigLogger_shouldNotBeLogged() {
        assertThat(TestAppender.contains(CONFIG_LOGGER_PREFIX)).isFalse();
    }

    @Test
    @DisplayName("profile properties are logged")
    void profilePropertiesAreLogged() {
        assertThat(TestAppender.contains("some-field: someValue")).isTrue();
    }

    @Test
    @DisplayName("redacted keys mask values")
    void testRedaction() {
        assertThat(TestAppender.contains("some-redacted-field: ***redacted***")).isTrue();
    }

    /**
     * The logger initializes on an ApplicationEnvironmentPreparedEvent. This means any initialization prior
     * to that gets lost(static initialization with BeforeAll). The {@link TestAppender} must get configured
     * after this event and before the {@link AppConfigurationLogger} gets called.
     */
    @TestComponent
    public static class AppenderInitializationHook implements GenericApplicationListener {

        // ensure that this called before the AppConfigurationLogger's ContextRefreshedEvent EventListener
        public int getOrder() {
            return HIGHEST_PRECEDENCE;
        }

        @Override
        public boolean supportsEventType(ResolvableType eventType) {
            return eventType.isAssignableFrom(ContextRefreshedEvent.class);
        }

        // initialize the TestAppender
        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            TestAppender.initialize(AppConfigurationLogger.class.getName(), Level.INFO);
        }
    }
}
