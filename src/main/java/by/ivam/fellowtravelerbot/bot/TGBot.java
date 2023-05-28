package by.ivam.fellowtravelerbot.bot;

import by.ivam.fellowtravelerbot.config.BotConfig;
import by.ivam.fellowtravelerbot.handler.RegistrationHandler;
import by.ivam.fellowtravelerbot.handler.StartHandler;
import by.ivam.fellowtravelerbot.handler.storages.StorageAccess;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Log4j
public class TGBot extends TelegramLongPollingBot {
    public TGBot(String botToken) {
        super(botToken);
    }

    @Autowired
    BotConfig botConfig;

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
        return botConfig.getBotName();
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
                    log.info("Start chat with " + incomeMessage.getChat().getUserName() + ". ChatId: " + chatId);
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
                    log.debug("get Message: " + update.getMessage().getText());
//                    sendMessage(prepareMessage(chatId, "Sorry this option still doesn't work"));

                    String chatStatus = storageAccess.findBotStatusFromRegUser(chatId);

                    log.debug("get chatStatus - " + chatStatus);
                    switch (chatStatus) {
                        case "NO_STATUS" -> unknownCommandReceived(chatId);
//                        case "REGISTRATION_START" -> {
//                        }
//                        case "REGISTRATION_WAIT_CONFIRMATION" -> {
//                        }
                        case "REGISTRATION_EDIT_NAME" -> {
                            log.info("Get edited name" +  messageText);

                            EditMessageText editMessageText = registrationHandler.confirmEditRegData(incomeMessage);
                            executeEditMessageText(editMessageText);
                        }
                    }

                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Message incomeMessage = update.getCallbackQuery().getMessage();
            int messageId = incomeMessage.getMessageId();
            long chatId = incomeMessage.getChatId();
            String userName = incomeMessage.getChat().getFirstName();


            if (callbackData.equals(buttons.getCONFIRM_START_REG_CALLBACK())) {
//  got confirmation of start registration process, call check up correctness of user firstname
                EditMessageText editMessageText = registrationHandler.checkRegData(messageId, chatId, userName);
                executeEditMessageText(editMessageText);
            } else if (callbackData.equals(buttons.getDENY_REG_CALLBACK())) {
//  got denial of registration process
// TODO вынести в метод отказа от регистрации в RegistrationHandler, реализовать возможность возврата в процесс регистрацц

                String answer = messages.getDENY_REG_DATA_MESSAGE();
                executeEditMessageText(answer, chatId, messageId);
            } else if (callbackData.equals(buttons.getCONFIRM_REG_DATA_CALLBACK())) {
//  got confirmation of correctness of user firstname, call saving to DB
                EditMessageText editMessageText = registrationHandler.userRegistration(incomeMessage);
                executeEditMessageText(editMessageText);
            } else if (callbackData.equals(buttons.getEDIT_REG_DATA_CALLBACK())) {
//  got request of edit of user firstname, call appropriate process
                EditMessageText editMessageText = registrationHandler.editUserName(incomeMessage);
                executeEditMessageText(editMessageText);
            }
//            ввод другого имени
//            else if (callbackData.equals(buttons.getNAME_CONFIRMED_CALLBACK())) {
//                EditMessageText editMessageText = registrationHandler.userRegistration(incomeMessage);
//                executeEditMessageText(editMessageText);
//            }

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
