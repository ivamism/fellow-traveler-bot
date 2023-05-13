package by.ivam.fellowtravelerbot.bot;

import by.ivam.fellowtravelerbot.handler.RegistrationHandler;
import by.ivam.fellowtravelerbot.handler.StartHandler;
import by.ivam.fellowtravelerbot.handler.storage.StorageAccess;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
@Log4j
public class TGBot extends TelegramLongPollingBot {
    public TGBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }


    @Value("${bot.name}")
    String botName;

    @Autowired
    StartHandler startHandler;

    @Autowired
    RegistrationHandler registrationHandler;

    @Autowired
    Keyboards keyboards;
    @Autowired
    Messages messages;

    @Autowired
    Buttons buttons;
    @Autowired
    StorageAccess storageAccess;


    @Override
    public String getBotUsername() {
        return botName;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message incomeMessage = update.getMessage();
            Integer messageId = incomeMessage.getMessageId();
            String messageText = incomeMessage.getText();
            long chatId = incomeMessage.getChatId();
            switch (messageText) {
                case "/start" -> {

                    startCommandReceived(chatId, incomeMessage.getChat().getFirstName());
                    log.info("Start chat with " + incomeMessage.getChat().getUserName()
                            + ". ChatId: " + chatId);
//                    startHandler.startMessaging(chatId, incomeMessage);

                    sendMessage(startHandler.startMessaging(chatId, incomeMessage));
                }
                case "/help" -> {

                    sendMessage(prepareMessage(chatId, messages.getHELP_TEXT()));
                    log.debug("get Message: " + messageText);

                }

                case "/registration" -> {
//                    log.debug("get Message: " + messageText + " - Start registration process");
//                    registerUser(chatId, );
                }
                default -> {
                    String chatStatus = storageAccess.findChatStatus(messageId);
                    switch (chatStatus) {
                        case "NO_STATUS" ->
                                unknownCommandReceived(chatId);
                    }


//                    sendMessage(prepareMessage(chatId, "Sorry this option still doesn't work"));
                    log.debug("get Message: " + update.getMessage().getText());
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Message incomeMessage = update.getCallbackQuery().getMessage();
            int messageId = incomeMessage.getMessageId();
            long chatId = incomeMessage.getChatId();
            String userName = incomeMessage.getChat().getFirstName();


            if (callbackData.equals(buttons.getCONFIRM_START_REG_CALLBACK())) {
                EditMessageText editMessageText = registrationHandler.checkRegData(messageId, chatId, userName);
                executeEditMessageText(editMessageText);
            } else if (callbackData.equals(buttons.getDENY_REG_CALLBACK())) {
                String answer = messages.getDENY_REG_DATA_MESSAGE();
                executeEditMessageText(answer, chatId, messageId);
            } else if (callbackData.equals(buttons.getCONFIRM_REG_DATA_CALLBACK())) {
                registrationHandler.userRegistration(incomeMessage);
                String answer = messages.getSUCCESS_REGISTRATION_MESSAGE();
                executeEditMessageText(answer, chatId, messageId);
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = "Привет, " + firstName + "!";
        sendMessage(prepareMessage(chatId, answer));
    }

    private void unknownCommandReceived(long chatId) {
        String answer = messages.getUNKNOWN_COMMAND();
        sendMessage(prepareMessage(chatId, answer));
    }

    private SendMessage prepareMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        message.setReplyMarkup(keyboards.mainMenu());
        return message;
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(messages.getERROR_TEXT() + e.getMessage());
        }
    }

    private void executeEditMessageText(String text, long chatId, int messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId(messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(messages.getERROR_TEXT() + e.getMessage());
        }
    }

    private void executeEditMessageText(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(messages.getERROR_TEXT() + e.getMessage());
        }
    }

}
