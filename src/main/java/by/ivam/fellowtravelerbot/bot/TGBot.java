package by.ivam.fellowtravelerbot.bot;

import by.ivam.fellowtravelerbot.handler.StartHandler;
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
    Keyboards keyboards;
    @Autowired
    Messages messages;


    @Override
    public String getBotUsername() {
        return botName;
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
                    sendMessage(prepareMessage(chatId, "Sorry this option still doesn't work"));
                    log.debug("get Message: " + update.getMessage().getText());
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String userName = update.getCallbackQuery().getMessage().getChat().getFirstName();

            switch (callbackData) {

                case "CONFIRM_REGISTRATION" -> {
                    String answer = messages.getCONFIRM_REG_DATA_MESSAGE() + userName + "?";
                    executeEditMessageText(answer, chatId, messageId);

                }

                case "DENY_REGISTRATION" -> {
                    String answer = messages.getDENY_REG_DATA_MESSAGE();
                    executeEditMessageText(answer, chatId, messageId);
                }

//            if(callbackData.equals(YES_BUTTON)){
//                String text = "You pressed YES button";
////
//            }
//            else if(callbackData.equals(NO_BUTTON)){
//                String text = "You pressed NO button";
//                executeEditMessageText(text, chatId, messageId);
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = "Привет, " + firstName + "!";
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

    private void executeEditMessageText(String text, long chatId, long messageId){
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(messages.getERROR_TEXT() + e.getMessage());
        }
    }
}
