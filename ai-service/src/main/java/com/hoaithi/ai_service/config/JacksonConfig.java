package com.hoaithi.ai_service.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;


@Configuration
public class JacksonConfig {

    // Set system property to ensure all Jackson instances use the new limit
    @PostConstruct
    public void configureJacksonGlobally() {
        System.setProperty("com.fasterxml.jackson.core.StreamReadConstraints.maxStringLength", "50000000");
        System.setProperty("com.fasterxml.jackson.core.StreamReadConstraints.maxNumberLength", "10000");
        System.setProperty("com.fasterxml.jackson.core.StreamReadConstraints.maxNestingDepth", "2000");
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        configureObjectMapper(mapper);
        return mapper;
    }

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder() {
            @Override
            public ObjectMapper build() {
                ObjectMapper mapper = super.build();
                configureObjectMapper(mapper);
                return mapper;
            }
        };
    }

    private void configureObjectMapper(ObjectMapper mapper) {
        // Configure stream read constraints for large responses
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxStringLength(500000000)    // 50MB
                .maxNumberLength(10000)         // Large numbers
                .maxNestingDepth(2000)          // Deep nesting
                .build();

        JsonFactory factory = mapper.getFactory();
        factory.setStreamReadConstraints(constraints);
    }
}