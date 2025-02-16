package com.github.sterrasi.spring.common.config;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * Logs the property sources on application startup
 */
@Component
@ConditionalOnProperty(name = "spring-common-service-lib.configuration-logger.enabled", havingValue = "true")
@EnableConfigurationProperties(AppConfigurationLogger.AppConfigurationLoggerProperties.class)
public class AppConfigurationLogger {

    private static final Logger log = LoggerFactory.getLogger(AppConfigurationLogger.class);

    static final String REDACTED_VALUE = "***redacted***";
    static final String BANNER = "------------ Application Configuration Properties ------------";

    static final String CONFIG_LOGGER_PREFIX = "spring-common-service-lib.configuration-logger";
    private final AppConfigurationLoggerProperties props;

    public AppConfigurationLogger(AppConfigurationLoggerProperties props){
        this.props = props;
        this.props.getExclusionKeys().add(CONFIG_LOGGER_PREFIX);
    }

    @EventListener
    public void handleContextInitializedEvent(ContextRefreshedEvent event) {

        var ctx = event.getApplicationContext();
        var env = ctx.getEnvironment();

        log.info(BANNER);
        log.info("Active Profiles: {}", Arrays.toString(env.getActiveProfiles()));
        var sources = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false)
                .filter(EnumerablePropertySource.class::isInstance)
                .forEach(ps -> {
                    var psName = ps.getName();
                    log.info("------------ Property Source: {} ------------", ps.getName());
                    Arrays.stream(((EnumerablePropertySource<?>) ps).getPropertyNames())
                            .distinct()
                            .filter(prop -> !props.getExclusionKeys().stream().anyMatch(
                                    key -> prop.contains(key)))
                            .sorted()
                            .forEach(prop -> {
                                log.info("{}: {}", prop, redactAsNecessary(prop, env));
                            });

                });
    }

    /**
     * Returns either the associated value of the given {@literal propName} or the {@link #REDACTED_VALUE} constant.
     * This depends on if the {@literal propName} contains any of the keys listed in the
     * {@link AppConfigurationLoggerProperties#getRedactionKeys()}
     *
     * @param propName name of the property to potentially redact
     * @param env      the Spring environment used to fetch the property value
     * @return property value from the given environment or {@link #REDACTED_VALUE}
     */
    private String redactAsNecessary(String propName, Environment env) {

        // The property should be redacted if it is in the 'redaction-keys' list
        if (props.getRedactionKeys().stream().anyMatch(key -> propName.contains(key))) {
            return REDACTED_VALUE;
        }
        return env.getProperty(propName);
    }

    /**
     * Configuration Properties for the {@link AppConfigurationLogger}
     */
    @ConfigurationProperties(prefix = "spring-common-service-lib.configuration-logger")
    @Data
    public static class AppConfigurationLoggerProperties {
        private boolean enabled = false;
        private Set<String> redactionKeys = new HashSet<>();
        private Set<String> exclusionKeys = new HashSet<>();
    }
}
