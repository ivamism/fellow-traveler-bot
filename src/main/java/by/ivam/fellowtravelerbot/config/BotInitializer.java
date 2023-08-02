package by.ivam.fellowtravelerbot.config;

import by.ivam.fellowtravelerbot.bot.Bot;
//import by.ivam.fellowtravelerbot.bot.TGBot;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
@Log4j
@Component
public class BotInitializer {

//    @Autowired
//    TGBot tgBot;

    @Autowired
    Bot bot;


//    @EventListener({ContextRefreshedEvent.class})
//    public void init() throws TelegramApiException {
//        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
//        try {
//            telegramBotsApi.registerBot(tgBot);
//        }
//        catch (TelegramApiException e) {
//            log.error("Error: " + e.getMessage());
//        }
//    }

    @EventListener({ContextRefreshedEvent.class})
    public void init2() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
        }
        catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }
}


