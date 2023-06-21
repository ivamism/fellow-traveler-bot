package by.ivam.fellowtravelerbot.bot;

import by.ivam.fellowtravelerbot.config.BotConfig;
import by.ivam.fellowtravelerbot.handler.CarHandler;
import by.ivam.fellowtravelerbot.handler.RegistrationHandler;
import by.ivam.fellowtravelerbot.handler.StartHandler;
import by.ivam.fellowtravelerbot.model.Car;
import by.ivam.fellowtravelerbot.storages.StorageAccess;
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
    CarHandler carHandler;
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
                case "/add_car" -> {
                    SendMessage message = carHandler.startAddCarProcess(incomeMessage);
                    sendMessage(message);
                }
                default -> {
                    log.debug("get Message: " + update.getMessage().getText());

                    String chatStatus = storageAccess.findChatStatus(chatId);

                    log.debug("get chatStatus - " + chatStatus);
                    switch (chatStatus) {
                        case "NO_STATUS" -> unknownCommandReceived(chatId);

                        case "REGISTRATION_EDIT_NAME" -> {
                            log.info("Get edited name " + messageText);
                            SendMessage message = registrationHandler.confirmEditedUserFirstName(incomeMessage);
                            sendMessage(message);
                        }
//                        case "ADD_CAR_VENDOR" -> {
//                            log.info("Get vendor " + messageText);
//                            carHandler.setVendor(chatId, messageText);
//                            SendMessage message = carHandler.requestModel(incomeMessage);
//                            sendMessage(message);
//                        }
                        case "ADD_CAR_MODEL" -> {
                            log.info("Get model " + messageText);
                            carHandler.setModel(chatId, messageText);
                            SendMessage message = carHandler.requestColor(incomeMessage);
                            sendMessage(message);
                        }
                        case "ADD_CAR_COLOR" -> {
                            log.info("Get color " + messageText);
                            carHandler.setColor(chatId, messageText);
                            SendMessage message = carHandler.requestPlateNumber(incomeMessage);
                            sendMessage(message);
                        }
                        case "ADD_CAR_PLATE" -> {
                            log.info("Get plate number " + messageText);
                            carHandler.setPlateNumber(chatId, messageText);
                            SendMessage message = carHandler.requestCommentary(incomeMessage);
                            sendMessage(message);
                        }
                        case "ADD_CAR_COMMENTARY" -> {
                            log.info("Get commentary " + messageText);
                            carHandler.setCommentary(chatId, messageText);
                            SendMessage message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
                            sendMessage(message);
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

            if (callbackData.equals(buttons.getCONFIRM_START_REG_CALLBACK())) {     //  got confirmation of start registration process, call check up correctness of user firstname

                EditMessageText editMessageText = registrationHandler.confirmUserFirstName(messageId, chatId, userName);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getDENY_REG_CALLBACK())) {   //  got denial of registration process

                EditMessageText editMessageText = registrationHandler.denyRegistration(incomeMessage);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getCONFIRM_REG_DATA_CALLBACK())) {   //  got confirmation of correctness of user firstname, call saving to DB

                EditMessageText editMessageText = registrationHandler.userRegistration(incomeMessage);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getEDIT_REG_DATA_CALLBACK())) {  //  got request of edit of user firstname, call appropriate process

                EditMessageText editMessageText = registrationHandler.editUserFirstName(incomeMessage);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getNAME_TO_CONFIRM_CALLBACK())) {  //  got confirmation of correctness of edited user firstname, call saving to DB
                String firstName = storageAccess.findUserFirstName(chatId);
                EditMessageText editMessageText = registrationHandler.userRegistration(incomeMessage, firstName);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getADD_CAR_START_DENY_CALLBACK())) {  //  deny add car process

                EditMessageText editMessageText = carHandler.denyStart(incomeMessage);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getADD_CAR_START_CALLBACK())) {   //  start add car process

                EditMessageText editMessageText = carHandler.requestModel(incomeMessage);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getADD_CAR_SKIP_COMMENT_CALLBACK())) {   //  add car process get skip commentary callback

                log.info("Get callback to skip commentary ");
                String emptyString = "";
                carHandler.setCommentary(chatId, emptyString);
                EditMessageText message = carHandler.checkDataBeforeSaveCarMessageSkipComment(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getADD_CAR_SAVE_CAR_CALLBACK())) { //  get callback to save car to DB

                log.info("get callback to save car to DB");
                Car car = carHandler.saveCar(chatId);
                EditMessageText message = carHandler.saveCarMessage(incomeMessage, car);
                sendEditMessage(message);
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = "Привет, " + firstName + "!";
        sendMessage(prepareMessage(chatId, answer));
        log.info("Start command received");
    }

    private void unknownCommandReceived(long chatId) {
        String answer = messages.getUNKNOWN_COMMAND();
        sendMessage(prepareMessage(chatId, answer));
        log.info("received unknown command");
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

    private void sendEditMessage(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(messages.getERROR_TEXT() + e.getMessage());
        }
    }
//    private void sendEditMessage(String text, long chatId, int messageId) {
//        EditMessageText message = new EditMessageText();
//        message.setChatId(chatId);
//        message.setText(text);
//        message.setMessageId(messageId);
//
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//            log.error(messages.getERROR_TEXT() + e.getMessage());
//        }
//    }
}
