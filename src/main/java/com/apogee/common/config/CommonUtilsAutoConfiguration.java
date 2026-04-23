package com.apogee.common.config;

import com.apogee.common.mapper.ReflectionObjectMapper;
import com.apogee.common.mapper.interfaces.ObjectMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Auto-configuration for common-utils.
 * Provides default beans for ObjectMapper and enables AOP.
 */
@Configuration
@EnableAspectJAutoProxy
public class CommonUtilsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ReflectionObjectMapper();
    }
}
