package by.ivam.fellowtravelerbot.redis.config;

import by.ivam.fellowtravelerbot.redis.subscriber.FindPassengerRequestCreationListener;
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


//    @Bean
//    public JedisConnectionFactory connectionFactory() {
//
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("127.0.0.1", 6379);
//        JedisConnectionFactory factory = new JedisConnectionFactory(configuration);
////        JedisConnectionFactory factory = new JedisConnectionFactory();
//        return factory;
//    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        template.setStringSerializer(RedisSerializer.string());
        return template;
    }
//        @Bean
//    public RedisTemplate<String, Object> redisTemplate() {
//        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
//        template.setConnectionFactory(connectionFactory());
//        template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
//        return template;
//    }


        @Bean
    public ChannelTopic chanelTopic() {
        return new ChannelTopic("__key*__:*");
    }
//    @Bean
//    public ChannelTopic expireTopic() {
//
//        return new ChannelTopic("__keyevent@0__:expired");
////        return new ChannelTopic("__keyevent@0__:expired");
//    }

    @Bean
    public MessageListenerAdapter messageFindPassengerRequestExpireListener() {
        return new MessageListenerAdapter(new MessageListener());
    }

    @Bean
    public MessageListenerAdapter messageFindPassengerRequestCreationListener() {
           return new MessageListenerAdapter(new FindPassengerRequestCreationListener());
    }


    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
//        PatternTopic patternTopic = new PatternTopic()
//        container.addMessageListener(messageFindPassengerRequestExpireListener(), chanelTopic());
        container.addMessageListener(messageFindPassengerRequestExpireListener(), new PatternTopic("__key*__:*"));
//        __key*__:*
//        container.addMessageListener(messageFindPassengerRequestCreationListener(), new PatternTopic("new_find_passenger_request"));
        return container;
    }
//    @Bean
//    MessagePublisher messagePublisher() {
//        return new MessagePublisherImpl(redisTemplate(), rideTopic());
//    }
//    @Bean
//    public MessageListenerAdapter messageListenerAdapter() {
//        return new MessageListenerAdapter(new MessageListener());
//    }


    //    @Bean
//    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
//                                            MessageListenerAdapter listenerAdapter) {
//
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.addMessageListener(listenerAdapter, new PatternTopic("chat"));
//
//        return container;
//    }
//    @Bean
//   public RedisMessageListenerContainer container() {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory());
//        container.addMessageListener(messageListenerAdapter(), rideTopic());
//        return container;
//    }

}
