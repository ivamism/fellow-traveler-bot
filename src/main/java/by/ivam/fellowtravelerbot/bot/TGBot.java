package by.ivam.fellowtravelerbot.bot;

import by.ivam.fellowtravelerbot.config.BotConfig;
import by.ivam.fellowtravelerbot.handler.CarHandler;
import by.ivam.fellowtravelerbot.handler.UserHandler;
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
    UserHandler userHandler;
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
                case "/profile", "Мои данные" -> {
                    log.debug("got request to get User's stored data");
                    SendMessage message = userHandler.sendUserData(chatId);
                    sendMessage(message);
                }
                case "/registration" -> {
                    log.debug("get Message: " + messageText + " - Start registration process");
//                    registerUser(chatId, );
                }
                case "/add_car" -> {
                    SendMessage message = carHandler.startAddCarProcess(incomeMessage);
                    sendMessage(message);
                }
                case "Найти машину" -> {

                    log.debug("got request to find a car");
                }
                case "Найти пассажира" -> {

                    log.debug("got request to find a fellow");
                }
                case "Помощь" -> {
                    sendMessage(prepareMessage(chatId, messages.getHELP_TEXT()));
                    log.debug("got request to get help and send help message");
                }

                default -> {
                    log.debug("get Message: " + update.getMessage().getText());

                    String chatStatus = storageAccess.findChatStatus(chatId);

                    log.debug("get chatStatus - " + chatStatus);
                    switch (chatStatus) {
                        case "NO_STATUS" -> unknownCommandReceived(chatId);

                        case "REGISTRATION_USER_EDIT_NAME" -> {
                            log.info("Get edited name " + messageText);
                            SendMessage message = userHandler.confirmEditedUserFirstName(incomeMessage);
                            sendMessage(message);
                        }

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
                        case "ADD_CAR_PLATES" -> {
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
                        case "ADD_CAR_EDIT_MODEL" -> {
                            log.info("Get edited model " + messageText);
                            carHandler.setEditedBeforeSavingModel(chatId, messageText);
                            SendMessage message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
                            sendMessage(message);
                        }
                        case "ADD_CAR_EDIT_COLOR" -> {
                            log.info("Get edited color " + messageText);
                            carHandler.setEditedBeforeSavingColor(chatId, messageText);
                            SendMessage message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
                            sendMessage(message);
                        }
                        case "ADD_CAR_EDIT_PLATES" -> {
                            log.info("Get edited plates " + messageText);
                            carHandler.setEditedBeforeSavingPlateNumber(chatId, messageText);
                            SendMessage message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
                            sendMessage(message);
                        }
                        case "ADD_CAR_EDIT_COMMENTARY" -> {
                            log.info("Get edited commentary " + messageText);
                            carHandler.setEditedBeforeSavingCommentary(chatId, messageText);
                            SendMessage message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
                            sendMessage(message);
                        }
                        case "USER_EDIT_NAME" -> {
                            log.info("Get edited User's firstname " + messageText);
                            userHandler.saveEditedUserFirstName(chatId, messageText);
                            SendMessage message = userHandler.editUserFirstNameSuccessMessage(chatId);
                            sendMessage(message);
                        }
                        case "EDIT_FIRST_CAR_MODEL" -> {
                            log.info("Get edited first car's model " + messageText);
                            Car car = carHandler.setFirstCarEditedModel(chatId, messageText);
                            SendMessage message = carHandler.editionCarSuccessMessage(chatId, car);
                            sendMessage(message);
                        }
                        case "EDIT_SECOND_CAR_MODEL" -> {
                            log.info("Get edited second car's model " + messageText);
                            Car car = carHandler.setSecondCarEditedModel(chatId, messageText);
                            SendMessage message = carHandler.editionCarSuccessMessage(chatId, car);
                            sendMessage(message);
                        }
                        case "EDIT_FIRST_CAR_COLOR" -> {
                            log.info("Get edited first car's color " + messageText);
                            Car car = carHandler.setFirstCarEditedColor(chatId, messageText);
                            SendMessage message = carHandler.editionCarSuccessMessage(chatId, car);
                            sendMessage(message);
                        }
                        case "EDIT_SECOND_CAR_COLOR" -> {
                            log.info("Get edited second car's color " + messageText);
                            Car car = carHandler.setSecondCarEditedColor(chatId, messageText);
                            SendMessage message = carHandler.editionCarSuccessMessage(chatId, car);
                            sendMessage(message);
                        }
                        case "EDIT_FIRST_CAR_PLATES" -> {
                            log.info("Get edited first car's plates " + messageText);
                            Car car = carHandler.setFirstCarEditedPlates(chatId, messageText);
                            SendMessage message = carHandler.editionCarSuccessMessage(chatId, car);
                            sendMessage(message);
                        }
                        case "EDIT_SECOND_CAR_PLATES" -> {
                            log.info("Get edited second car's plates " + messageText);
                            Car car = carHandler.setSecondCarEditedPlates(chatId, messageText);
                            SendMessage message = carHandler.editionCarSuccessMessage(chatId, car);
                            sendMessage(message);
                        }
                        case "EDIT_FIRST_CAR_COMMENTARY" -> {
                            log.info("Get edited first car's commentary " + messageText);
                            Car car = carHandler.setFirstCarEditedCommentary(chatId, messageText);
                            SendMessage message = carHandler.editionCarSuccessMessage(chatId, car);
                            sendMessage(message);
                        }
                        case "EDIT_SECOND_CAR_COMMENTARY" -> {
                            log.info("Get edited second car's commentary " + messageText);
                            Car car = carHandler.setSecondCarEditedCommentary(chatId, messageText);
                            SendMessage message = carHandler.editionCarSuccessMessage(chatId, car);
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
            String messageText = incomeMessage.getText();
            String userName = incomeMessage.getChat().getFirstName();

            if (callbackData.equals(buttons.getCONFIRM_START_REG_CALLBACK())) {     //  got confirmation of start registration process, call check up correctness of user firstname
                EditMessageText editMessageText = userHandler.confirmUserFirstName(messageId, chatId, userName);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getDENY_REG_CALLBACK())) {   //  got denial of registration process
                EditMessageText editMessageText = userHandler.denyRegistration(incomeMessage);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getCONFIRM_REG_DATA_CALLBACK())) {   //  got confirmation of correctness of user firstname, call saving to DB
                EditMessageText editMessageText = userHandler.userRegistration(incomeMessage);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getEDIT_REG_DATA_CALLBACK())) {  //  got request of edit of user firstname, call appropriate process
                EditMessageText editMessageText = userHandler.editUserFirstNameBeforeSaving(incomeMessage);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getNAME_TO_CONFIRM_CALLBACK())) {  //  got confirmation of correctness of edited user firstname, call saving to DB
                String firstName = storageAccess.findUserFirstName(chatId);
                EditMessageText editMessageText = userHandler.userRegistration(incomeMessage, firstName);
                sendEditMessage(editMessageText);
            } else if (callbackData.equals(buttons.getADD_CAR_START_DENY_CALLBACK())) {  //  deny add car process
                EditMessageText editMessageText = carHandler.quitProcessMessage(incomeMessage);
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
            } else if (callbackData.equals(buttons.getCANCEL_CALLBACK())) { //  callback to exit delete car process
                log.info("get callback to exit delete car process");
                EditMessageText message = carHandler.denyDeleteCarMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getREQUEST_DELETE_CAR_CALLBACK())) { //  callback to start delete car process
                log.info("get callback to exit delete car process");
                EditMessageText message = carHandler.sendCarListToDelete(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getDELETE_FIRST_CAR_CALLBACK())) { //  callback to delete first car from list
                log.info("callback to delete car first car from list");
                String deleteFirstCar = carHandler.deleteFirstCar(chatId);
                EditMessageText message = carHandler.deleteCarMessage(incomeMessage, deleteFirstCar);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getDELETE_SECOND_CAR_CALLBACK())) { //  callback to delete second car from list
                log.info("callback to delete car second car from list");
                String deleteSecondCar = carHandler.deleteSecondCar(chatId);
                EditMessageText message = carHandler.deleteCarMessage(incomeMessage, deleteSecondCar);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getDELETE_ALL_CARS_CALLBACK())) { //  callback to delete all cars from list
                log.info("callback to delete all cars from list");
                carHandler.deleteTwoCars(chatId);
                EditMessageText message = carHandler.deleteAllCarsMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getADD_CAR_EDIT_CAR_CALLBACK())) { //  callback to edit car before saving
                log.info("callback to edit car before saving");
                EditMessageText message = carHandler.editCarBeforeSavingStartMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getADD_CAR_EDIT_MODEL_CALLBACK())) { //  callback to edit car's model before saving
                log.info("callback to edit car's model before saving");
                EditMessageText message = carHandler.changeModelBeforeSavingRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getADD_CAR_EDIT_COLOR_CALLBACK())) { //  callback to edit car's color before saving
                log.info("callback to edit car's color before saving");
                EditMessageText message = carHandler.changeColorBeforeSavingRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getADD_CAR_EDIT_PLATES_CALLBACK())) { //  callback to edit car's plate number before saving
                log.info("callback to edit car's plate number before saving");
                EditMessageText message = carHandler.changePlateNumberBeforeSavingRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getADD_CAR_EDIT_COMMENTARY_CALLBACK())) { //  callback to edit car's commentary before saving
                log.info("callback to edit car's commentary before saving");
                EditMessageText message = carHandler.changeCommentaryBeforeSavingRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_USER_NAME_CALLBACK())) { //  callback to edit user's name
                log.info("callback to edit user's name");
                EditMessageText message = userHandler.editUserFirstNameMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_CAR_START_PROCESS_CALLBACK())) { //  callback to edit user's cars
                log.info("callback to edit user's cars");
                EditMessageText message = carHandler.sendCarListToEdit(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_CAR_CHOOSE_FIRST_CAR_CALLBACK())) { //  callback to edit first car
                log.info("callback to edit first car");
                EditMessageText message = carHandler.editFirstCarMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_CAR_CHOOSE_SECOND_CAR_CALLBACK())) { //  callback to edit second car
                log.info("callback to edit second car");
                EditMessageText message = carHandler.editSecondCarMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_FIRST_CAR_EDIT_MODEL_CALLBACK())) { //  callback to edit first car's model
                log.info("callback to edit first car's model");
                EditMessageText message = carHandler.changeFirstCarModelRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_SECOND_CAR_EDIT_MODEL_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit second car's model");
                EditMessageText message = carHandler.changeSecondCarModelRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_FIRST_CAR_EDIT_COLOR_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit first car's color");
                EditMessageText message = carHandler.changeFirstCarColorRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_SECOND_CAR_EDIT_COLOR_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit second car's color");
                EditMessageText message = carHandler.changeSecondCarColorRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_FIRST_CAR_EDIT_PLATES_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit first car's plates");
                EditMessageText message = carHandler.changeFirstCarPlatesRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_SECOND_CAR_EDIT_PLATES_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit second car's plates");
                EditMessageText message = carHandler.changeSecondCarPlatesRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_FIRST_CAR_EDIT_COMMENTARY_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit first car's commentary");
                EditMessageText message = carHandler.changeFirstCarCommentaryRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getEDIT_SECOND_CAR_EDIT_COMMENTARY_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit second car's commentary");
                EditMessageText message = carHandler.changeSecondCarCommentaryRequestMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getDELETE_USER_START_PROCESS_CALLBACK())) { //  callback start deletion User's stored data
                log.info("callback to start deletion User's stored data");
                EditMessageText message = userHandler.deleteUserStartProcessMessage(incomeMessage);
                sendEditMessage(message);
            } else if (callbackData.equals(buttons.getDELETE_USER_CONFIRM_CALLBACK())) { //  callback to delete User's stored data
                log.info("callback to delete User's stored data");
                EditMessageText message = userHandler.deleteUserSuccessMessage(incomeMessage);
                userHandler.deleteUser(chatId);
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
}
