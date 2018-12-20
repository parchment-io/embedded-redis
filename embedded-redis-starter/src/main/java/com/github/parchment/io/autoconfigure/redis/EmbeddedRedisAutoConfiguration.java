package com.github.parchment.io.autoconfigure.redis;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

@Configuration
@ConditionalOnProperty(prefix = "spring.redis.embedded", name="enabled", havingValue = "true")
public class EmbeddedRedisAutoConfiguration {
    @Autowired
    private RedisProperties redisProperties;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        redisServer = RedisServer
                .builder()
                .port(redisProperties.getPort())
                .build();
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
    }
}
