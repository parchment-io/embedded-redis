package com.github.parchment.io.autoconfigure.redis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

@Configuration("embeddedRedisAutoConfiguration")
@ConditionalOnProperty(prefix = "spring.redis.embedded", name="enabled", havingValue = "true")
public class EmbeddedRedisAutoConfiguration {
    @Autowired
    private RedisProperties redisProperties;

    private RedisServer redisServer;

    /**
     * The purpose of this is to enforce a dependency on Spring Data Redis auto config and Redisson auto config
     * so that we will start the embedded server before those configurations are processed. Spring data probably
     * does not matter as much, but Redisson will fail startup if the server isn't already running.
     */
    @Bean
    public static BeanFactoryPostProcessor dependsOnPostProcessor() {
        return new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
                    throws BeansException {
                for (String beanName : BeanFactoryUtils.beanNamesIncludingAncestors(beanFactory)) {
                    if ("org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration".equalsIgnoreCase(beanName)
                            || "org.redisson.spring.starter.RedissonAutoConfiguration".equalsIgnoreCase(beanName)) {
                        BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
                        definition.setDependsOn(StringUtils.addStringToArray(
                                definition.getDependsOn(), "embeddedRedisAutoConfiguration"));
                    }
                }
            }
        };
    }

    @PostConstruct
    public void startRedis() {
        RedisServerBuilder builder = RedisServer.builder();

        /*
         * If we've set a spring.redis.password, enable that in the embedded server. Redisson will send AUTH
         * from the client regardless so apis using redisson require this. The requirepass cannot be passed on
         * the command line, so we create a minimal conf file.
         */
        List<String> props = new ArrayList<>();
        if (!StringUtils.isEmpty(redisProperties.getPassword())) {
            props.add("requirepass " + redisProperties.getPassword() + "\n");
        }
        // future props added here...

        if (props.size() > 0) {
            builder.configFile(writeRedisConf(props).getAbsolutePath());
        }

        redisServer = builder
                .port(redisProperties.getPort())
                .build();
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
    }

    private File writeRedisConf(List<String> properties) {
        FileWriter fileWriter = null;
        File tempConf = null;
        try {
            tempConf = File.createTempFile("redis", "conf");
            tempConf.deleteOnExit();
            fileWriter = new FileWriter(tempConf);
            for (String prop : properties) {
                fileWriter.write(prop);
            }
            fileWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }

        return tempConf;
    }
}
