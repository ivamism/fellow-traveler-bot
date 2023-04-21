package by.ivam.fellowtravelerbot.bot;

import by.ivam.fellowtravelerbot.config.BotConfig;
import by.ivam.fellowtravelerbot.handler.RegistrationHandler;
import by.ivam.fellowtravelerbot.servise.CarService;
import by.ivam.fellowtravelerbot.servise.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
@Data
@Log4j
public class TGBot extends TelegramLongPollingBot {

    @Autowired
    BotConfig botConfig;

    @Autowired
    RegistrationHandler registrationHandler;

    @Autowired
    CarService carService;

    @Autowired
    UserService userService;

    @Autowired
    Keyboards keyboards;
    @Autowired
    Messages messages;


    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }


    public String getBotToken() {
        return botConfig.getBotToken();
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message incomeMessage = update.getMessage();
            String messageText = incomeMessage.getText();
            long chatId = incomeMessage.getChatId();
            switch (messageText) {
                case "/start" -> {
                    startCommandReceived(chatId, incomeMessage.getChat().getFirstName());
                    log.info("Start Bot with " + incomeMessage.getChat().getUserName()
                            + ". ChatId: " + chatId);
                    if (userService.findById(chatId).isEmpty()) {
                        registrationHandler.startRegistration(incomeMessage);
                    } else {
                        String answer = messages.getCHOOSE_ACTION();

                        sendMessage(chatId, answer);
                    }

                }
                case "/help" -> {
                    sendMessage(chatId, messages.getHELP_TEXT());
                    log.debug("get Message: " + messageText);
                }

                case "/registration" -> {
//                    log.debug("get Message: " + messageText + " - Start registration process");
//                    registerUser(chatId, );
                }
                default -> {
                    sendMessage(chatId, "Sorry this option still doesn't work");
                    log.debug("get Message: " + update.getMessage().getText());
                }
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = "Hi," + firstName + "!";

        sendMessage(chatId, answer);

    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void registerUser(long chatId) {


//        {
//            SendMessage message = new SendMessage();
////            String userFirstName =
//            message.setChatId((chatId));
//            message.setText("Поддвердите данные для регистрации: \n\n Ваше Имя - " +);
//        }


//        message.setReplyMarkup(markupInLine);

//        executeMessage(message);
    }

     void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(messages.getERROR_TEXT() + e.getMessage());
        }
    }
}
