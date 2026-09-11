package com.coffee_shop.coffee_shop.config;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory
    ) {

        GenericJacksonJsonRedisSerializer genericSerializer =
                GenericJacksonJsonRedisSerializer.builder()
                        .build();

        JacksonJsonRedisSerializer<PageDTO> pageDTOSerializer =
                new JacksonJsonRedisSerializer<>(PageDTO.class);

        RedisCacheConfiguration baseConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(5))
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(genericSerializer)
                        );

        RedisCacheConfiguration productPaginationConfig =
                baseConfig
                        .entryTtl(Duration.ofMinutes(3))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(pageDTOSerializer)
                        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withCacheConfiguration(
                        "productPagination",
                        productPaginationConfig
                )
                .build();
    }
}