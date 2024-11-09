package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {


    /*
    在配置类中，方法参数可以直接从 Spring 容器中获取 Bean，而不需要 @Autowired 注解。
    Spring 会自动将已存在的 RedisConnectionFactory Bean 作为参数注入到 redisTemplate 方法中，
    因为 Spring 会在容器中查找类型匹配的 Bean 并自动提供。
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redis模版对象");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象，这个连接工厂对象在导入redis的依赖之后就自动地被spring给创建好了，并放到了spring容器里面
        //该行代码将传入的 redisConnectionFactory 设置为 RedisTemplate 的连接工厂，用来连接 Redis。
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        /*
        设置redis key的序列化器
        这行代码指定了 Redis 的 key 序列化器，将其设置为 StringRedisSerializer。
        在 Redis 中，所有数据都以字节数组存储。StringRedisSerializer 将 key 序列化为字符串，这样 Redis 中的 key 就以可读的字符串存储，便于开发和调试。
        若未设置序列化器，Redis 默认以 Java 的序列化方式保存数据，这会导致 key 显示为不可读的二进制数据。
        设置这是为了能够在图形化界面更清晰的展示key的值，方便开发的时候进行代码的调试，不然的话，key在图形化界面展示的是乱码
         */
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;
    }

}
