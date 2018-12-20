package com.github.parchment.io.autoconfigure.redis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = "spring.redis.embedded.enabled=false")
public class EmbeddedRedisAutoConfigurationDisabledTest {
    private static String KEY = "mykey";
    private static String VALUE = "myvalue";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test(expected = RedisConnectionFailureException.class)
    public void test_When_EmbeddedIsEnabled_Expect_ToReceiveExceptionTryingToConnect() {
        // set some value
        redisTemplate.boundValueOps(KEY).set(VALUE);

        // make sure we can read the value-
        assertEquals(VALUE, redisTemplate.boundValueOps(KEY).get());
    }
}
