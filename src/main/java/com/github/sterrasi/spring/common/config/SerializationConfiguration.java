package com.github.sterrasi.spring.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sterrasi.spring.common.ObjectMapperProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.Clock;

/**
 * Custom {@link ObjectMapper}
 */
@Configuration
public class SerializationConfiguration {

    @Bean
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }

    /**
     * Use an application specific {@link ObjectMapper}
     */
    @Bean
    @Primary
    public ObjectMapper getObjectMapper() {
        return ObjectMapperProvider.get();
    }

    /**
     * Use the application specific {@link ObjectMapper} in the Web MVC
     * {@link org.springframework.http.converter.HttpMessageConverter}.
     *
     * @param objectMapper custom mapper for the application
     * @return converter with the custom mapper.
     */
    @Bean
    @Autowired
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
