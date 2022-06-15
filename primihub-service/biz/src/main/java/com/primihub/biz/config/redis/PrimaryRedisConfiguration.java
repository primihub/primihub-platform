package com.primihub.biz.config.redis;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Getter
@Setter
@Slf4j
@Configuration
@EnableRedisRepositories(basePackages = {"com.primihub.biz.repository.primaryredis.*"})
public class PrimaryRedisConfiguration {

    @NacosValue(value="${spring.datasource.redis.primary.hostName}",autoRefreshed = true)
    private String hostName;
    @NacosValue(value="${spring.datasource.redis.primary.port}",autoRefreshed = true)
    private int port;
    @NacosValue(value="${spring.datasource.redis.primary.password}",autoRefreshed = true)
    private String password;
    @NacosValue(value="${spring.datasource.redis.primary.database}",autoRefreshed = true)
    private int database;

    @Bean(name = "primaryRedisPoolConfig")
    @Primary
    public JedisPoolConfig JedisPoolConfig(){
        return new PrimaryRedisPoolConfigWrapper();
    }

    @Bean(name = "primaryRedisConnectionFactory")
    @Primary
    public RedisConnectionFactory connectionFactory(@Qualifier("primaryRedisPoolConfig") JedisPoolConfig jedisPoolConfig){
        JedisConnectionFactory factory=new JedisConnectionFactory(jedisPoolConfig);
        factory.getStandaloneConfiguration().setHostName(hostName);
        factory.getStandaloneConfiguration().setPort(port);
        factory.getStandaloneConfiguration().setPassword(password);
        factory.getStandaloneConfiguration().setDatabase(database);
        return factory;
    }

    @Bean(name = "primaryStringRedisTemplate")
    @Primary
    public StringRedisTemplate stringRedisTemplate(@Qualifier("primaryRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory){
        StringRedisTemplate stringRedisTemplate=new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        StringRedisSerializer stringRedisSerializer=new StringRedisSerializer();
        stringRedisTemplate.setKeySerializer(stringRedisSerializer);
        stringRedisTemplate.setValueSerializer(stringRedisSerializer);
        stringRedisTemplate.setHashKeySerializer(stringRedisSerializer);
        stringRedisTemplate.setHashValueSerializer(stringRedisSerializer);
        return stringRedisTemplate;
    }
}
