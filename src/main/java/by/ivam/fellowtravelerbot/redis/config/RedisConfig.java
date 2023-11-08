package by.ivam.fellowtravelerbot.redis.config;

import by.ivam.fellowtravelerbot.redis.subscriber.MessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        template.setStringSerializer(RedisSerializer.string());
        return template;
    }

    @Bean
    public ChannelTopic chanelTopic() {
        return new ChannelTopic("__key*__:*");
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(MessageListener messageListener) {
        return new MessageListenerAdapter(messageListener);
//        return new MessageListenerAdapter(new MessageListener());
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
//        container.addMessageListener(messageListenerAdapter(), chanelTopic());
        container.addMessageListener(messageListenerAdapter(new MessageListener()), new PatternTopic("__key*__:*"));
        return container;
    }

    @Bean
    MessageListener messageListener() {
        return new MessageListener();
    }
//    @Bean
//    MessagePublisher messagePublisher() {
//        return new MessagePublisherImpl(redisTemplate(), rideTopic());
//    }

}
