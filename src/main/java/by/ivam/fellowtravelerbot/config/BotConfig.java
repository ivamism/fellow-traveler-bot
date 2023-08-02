package by.ivam.fellowtravelerbot.config;

import by.ivam.fellowtravelerbot.bot.Bot;
//import by.ivam.fellowtravelerbot.bot.TGBot;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.properties")
@Data
public class BotConfig {
    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String botToken;

//    @Bean
//    public TGBot tgBot() {
//        return new TGBot(botToken);
//    }
    @Bean
    public Bot bot() {
        return new Bot(botToken);
    }
}
