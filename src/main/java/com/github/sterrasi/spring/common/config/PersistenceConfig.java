package com.github.sterrasi.spring.common.config;


import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

/**
 * Enables R2DBC Auditing and Repositories.
 */
@ConditionalOnClass(ConnectionFactory.class)
@EnableR2dbcAuditing
@Configuration
public class PersistenceConfig {

    /**
     * Returns a {@link DateTimeProvider} tht uses the application's {@link Clock}.
     * @param clock the {@link Clock} from the {@link org.springframework.context.ApplicationContext}
     */
    @Bean("r2dbcDateTimeAuditingProvider")
    public DateTimeProvider getDateTimeProvider(Clock clock){
        return new R2dbcDateTimeAuditingProvider(clock);
    }

    /**
     * A {@link DateTimeProvider} that uses the {@link Clock} from the {@link org.springframework.context.ApplicationContext}
     */
    private record R2dbcDateTimeAuditingProvider(Clock clock) implements DateTimeProvider{
        @Override
        public Optional<TemporalAccessor> getNow() {
            return Optional.of(LocalDateTime.now(clock));
        }
    }
}
