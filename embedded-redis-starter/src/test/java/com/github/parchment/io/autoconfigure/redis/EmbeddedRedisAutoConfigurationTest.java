package com.github.parchment.io.autoconfigure.redis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmbeddedRedisAutoConfigurationTest {
    private static String KEY = "mykey";
    private static String VALUE = "myvalue";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void test_When_EmbeddedIsEnabled_Expect_ToReadAndWriteSuccessfully() {
        // set some value
        redisTemplate.boundValueOps(KEY).set(VALUE);

        // make sure we can read the value-
        assertEquals(VALUE, redisTemplate.boundValueOps(KEY).get());
    }
}
