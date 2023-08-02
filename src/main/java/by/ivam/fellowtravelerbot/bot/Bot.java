package by.ivam.fellowtravelerbot.bot;

import by.ivam.fellowtravelerbot.config.BotConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
@Log4j
public class Bot extends TelegramLongPollingBot {
    public Bot(String botToken) {
        super(botToken);
    }
    @Autowired
    BotConfig botConfig;
    @Autowired
   Messages messages;
    @Autowired
    ResponseMessageProcessor messageProcessor;

    @Autowired
    MessageDispatcher messageDispatcher;

    @PostConstruct
    public void init() {
        messageProcessor.setBot(this);

    }
    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()){
            messageDispatcher.onMessageReceived(update.getMessage());
        }

    }
    public  void sendMessage(SendMessage message) {

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(messages.getERROR_TEXT() + e.getMessage());
        }
    }

    public void sendEditMessage(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error(messages.getERROR_TEXT() + e.getMessage());
        }
    }

}
