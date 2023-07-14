package by.ivam.fellowtravelerbot.bot;

import by.ivam.fellowtravelerbot.config.BotConfig;
import by.ivam.fellowtravelerbot.model.DepartureLocation;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.servise.handler.AdminHandler;
import by.ivam.fellowtravelerbot.servise.handler.CarHandler;
import by.ivam.fellowtravelerbot.servise.handler.UserHandler;
import by.ivam.fellowtravelerbot.servise.handler.StartHandler;
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
// TODO найти причину того, что иногда предыдущее сообщения высылаются добавочно к новому

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
    AdminHandler adminHandler;
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

    SendMessage message = new SendMessage();
    EditMessageText editMessageText = new EditMessageText();


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
                    message = startHandler.startMessaging(chatId, incomeMessage);
                }
                case "/showMasterAdminMenu" -> {
                    log.debug("get Message: " + messageText + " - request to send admin menu from user " + chatId);
                    adminCommandReceived(chatId);
                }
                case "/help" -> {
                    message = prepareMessage(chatId, messages.getHELP_TEXT());
                    log.debug("get Message: " + messageText);
                }
                case "/profile", "Мои данные" -> {
                    if (startHandler.checkRegistration(chatId)) {
                        message = startHandler.noRegistrationMessage(chatId);
                    } else {
                        log.debug("got request to get User's stored data");
                        message = userHandler.sendUserData(chatId);
                    }
                }
                case "/registration" -> {
                    message = startHandler.startMessaging(chatId, incomeMessage);
                    log.debug("get Message: " + messageText + " - Start registration process");
                }
                case "/add_car" -> {
                    if (startHandler.checkRegistration(chatId)) {
                        message = startHandler.noRegistrationMessage(chatId);
                    } else {
                        log.debug("got request to get User's stored data");
                        message = carHandler.startAddCarProcess(incomeMessage);
                    }
                }
                case "Найти попутку" -> {
                    if (startHandler.checkRegistration(chatId)) {
                        message = startHandler.noRegistrationMessage(chatId);
                    } else {
                        log.debug("got request to find a car");
                    }
                }
                case "Найти попутчика" -> {
//                    SendMessage message = new SendMessage();
                    if (startHandler.checkRegistration(chatId)) {
                        message = startHandler.noRegistrationMessage(chatId);
                    } else {
                        log.debug("got request to find a fellow");
                    }
                }
                case "Помощь" -> {
                    sendMessage(prepareMessage(chatId, messages.getHELP_TEXT()));
                    log.debug("got request to get help and send help message");
                }
                case "Добавить нас. пункт" -> {
                    log.debug("got request to add new Settlement");
                    if (adminHandler.checkIsAdmin(chatId)) {
                        message = adminHandler.settlementNameRequestMessage(chatId);
                    } else {
                        log.debug("user " + chatId + " not an Admin");
                        unknownCommandReceived(chatId);
                    }
                }
                case "Добавить локацию" -> {
                    log.debug("got request to add new DepartureLocation");
                    if (adminHandler.checkIsAdmin(chatId)) {
                        message = adminHandler.departureLocationSettlementRequestMessage(chatId);
//                        adminHandler.departureLocationSettlementRequestMessage(chatId);
                    } else {
                        log.debug("user " + chatId + " not an Admin");
                        unknownCommandReceived(chatId);
                    }
                }
                default -> {
                    log.debug("get Message: " + update.getMessage().getText());

                    String chatStatus = storageAccess.findChatStatus(chatId);

                    log.debug("get chatStatus - " + chatStatus);
                    switch (chatStatus) {
                        case "NO_STATUS" -> unknownCommandReceived(chatId);

                        case "REGISTRATION_USER_EDIT_NAME" -> {
                            log.info("Get edited name " + messageText);
                            message = userHandler.confirmEditedUserFirstName(incomeMessage);
                        }

                        case "ADD_CAR_MODEL" -> {
                            log.info("Get model " + messageText);
                            carHandler.setModel(chatId, messageText);
                            message = carHandler.requestColor(incomeMessage);
                        }
                        case "ADD_CAR_COLOR" -> {
                            log.info("Get color " + messageText);
                            carHandler.setColor(chatId, messageText);
                            message = carHandler.requestPlateNumber(incomeMessage);
                        }
                        case "ADD_CAR_PLATES" -> {
                            log.info("Get plate number " + messageText);
                            carHandler.setPlateNumber(chatId, messageText);
                            message = carHandler.requestCommentary(incomeMessage);
                        }
                        case "ADD_CAR_COMMENTARY" -> {
                            log.info("Get commentary " + messageText);
                            carHandler.setCommentary(chatId, messageText);
                            message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
                        }
                        case "ADD_CAR_EDIT_MODEL" -> {
                            log.info("Get edited model " + messageText);
                            carHandler.setEditedBeforeSavingModel(chatId, messageText);
                            message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
                        }
                        case "ADD_CAR_EDIT_COLOR" -> {
                            log.info("Get edited color " + messageText);
                            carHandler.setEditedBeforeSavingColor(chatId, messageText);
                            message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
                        }
                        case "ADD_CAR_EDIT_PLATES" -> {
                            log.info("Get edited plates " + messageText);
                            carHandler.setEditedBeforeSavingPlateNumber(chatId, messageText);
                            message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
                        }
                        case "ADD_CAR_EDIT_COMMENTARY" -> {
                            log.info("Get edited commentary " + messageText);
                            carHandler.setEditedBeforeSavingCommentary(chatId, messageText);
                            message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
                        }
                        case "USER_EDIT_NAME" -> {
                            log.info("Get edited User's firstname " + messageText);
                            userHandler.saveEditedUserFirstName(chatId, messageText);
                            message = userHandler.editUserFirstNameSuccessMessage(chatId);
                        }
                        case "EDIT_FIRST_CAR_MODEL" -> {
                            log.info("Get edited first car's model " + messageText);
                            Car car = carHandler.setFirstCarEditedModel(chatId, messageText);
                            message = carHandler.editionCarSuccessMessage(chatId, car);
                        }
                        case "EDIT_SECOND_CAR_MODEL" -> {
                            log.info("Get edited second car's model " + messageText);
                            Car car = carHandler.setSecondCarEditedModel(chatId, messageText);
                            message = carHandler.editionCarSuccessMessage(chatId, car);
                        }
                        case "EDIT_FIRST_CAR_COLOR" -> {
                            log.info("Get edited first car's color " + messageText);
                            Car car = carHandler.setFirstCarEditedColor(chatId, messageText);
                            message = carHandler.editionCarSuccessMessage(chatId, car);
                        }
                        case "EDIT_SECOND_CAR_COLOR" -> {
                            log.info("Get edited second car's color " + messageText);
                            Car car = carHandler.setSecondCarEditedColor(chatId, messageText);
                            message = carHandler.editionCarSuccessMessage(chatId, car);
                        }
                        case "EDIT_FIRST_CAR_PLATES" -> {
                            log.info("Get edited first car's plates " + messageText);
                            Car car = carHandler.setFirstCarEditedPlates(chatId, messageText);
                            message = carHandler.editionCarSuccessMessage(chatId, car);
                        }
                        case "EDIT_SECOND_CAR_PLATES" -> {
                            log.info("Get edited second car's plates " + messageText);
                            Car car = carHandler.setSecondCarEditedPlates(chatId, messageText);
                            message = carHandler.editionCarSuccessMessage(chatId, car);
                        }
                        case "EDIT_FIRST_CAR_COMMENTARY" -> {
                            log.info("Get edited first car's commentary " + messageText);
                            Car car = carHandler.setFirstCarEditedCommentary(chatId, messageText);
                            message = carHandler.editionCarSuccessMessage(chatId, car);
                        }
                        case "EDIT_SECOND_CAR_COMMENTARY" -> {
                            log.info("Get edited second car's commentary " + messageText);
                            Car car = carHandler.setSecondCarEditedCommentary(chatId, messageText);
                            message = carHandler.editionCarSuccessMessage(chatId, car);
                        }
                        case "ADD_SETTLEMENT_NAME" -> {
                            log.info("Get Settlement name  " + messageText);
                            Settlement settlement = adminHandler.saveSettlement(chatId, messageText);
                            message = adminHandler.settlementSaveSuccessMessage(chatId, settlement);
                        }
                        case "ADD_DEPARTURE_LOCATION_NAME" -> {
                            log.info("Get DepartureLocation name  " + messageText);
                            DepartureLocation location = adminHandler.departureLocationSave(chatId, messageText);
                            message = adminHandler.departureLocationSaveSuccessMessage(chatId, location);
                        }
                    }
                }
            }
            sendMessage(message);

        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Message incomeMessage = update.getCallbackQuery().getMessage();
            int messageId = incomeMessage.getMessageId();
            long chatId = incomeMessage.getChatId();
            String messageText = incomeMessage.getText();
            String userName = incomeMessage.getChat().getFirstName();
            log.info("get callback: " + callbackData);

            if (callbackData.equals(buttons.getCONFIRM_START_REG_CALLBACK())) {     //  got confirmation of start registration process, call check up correctness of user firstname
                editMessageText = userHandler.confirmUserFirstName(messageId, chatId, userName);

            } else if (callbackData.equals(buttons.getDENY_REG_CALLBACK())) {   //  got denial of registration process
                editMessageText = userHandler.denyRegistration(incomeMessage);

            } else if (callbackData.equals(buttons.getCONFIRM_REG_DATA_CALLBACK())) {   //  got confirmation of correctness of user firstname, call saving to DB
                editMessageText = userHandler.userRegistration(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_REG_DATA_CALLBACK())) {  //  got request of edit of user firstname, call appropriate process
                editMessageText = userHandler.editUserFirstNameBeforeSaving(incomeMessage);

            } else if (callbackData.equals(buttons.getNAME_TO_CONFIRM_CALLBACK())) {  //  got confirmation of correctness of edited user firstname, call saving to DB
                String firstName = storageAccess.findUserFirstName(chatId);
                editMessageText = userHandler.userRegistration(incomeMessage, firstName);

            } else if (callbackData.equals(buttons.getADD_CAR_START_DENY_CALLBACK())) {  //  deny add car process
                editMessageText = carHandler.quitProcessMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getADD_CAR_START_CALLBACK())) {   //  start add car process
                editMessageText = carHandler.requestModel(incomeMessage);

            } else if (callbackData.equals(buttons.getADD_CAR_SKIP_COMMENT_CALLBACK())) {   //  add car process get skip commentary callback
                log.info("Get callback to skip commentary ");
                String emptyString = "";
                carHandler.setCommentary(chatId, emptyString);
                editMessageText = carHandler.checkDataBeforeSaveCarMessageSkipComment(incomeMessage);

            } else if (callbackData.equals(buttons.getADD_CAR_SAVE_CAR_CALLBACK())) { //  get callback to save car to DB
                log.info("get callback to save car to DB");
                Car car = carHandler.saveCar(chatId);
                editMessageText = carHandler.saveCarMessage(incomeMessage, car);

            } else if (callbackData.equals(buttons.getCANCEL_CALLBACK())) { //  callback to exit delete car process
                log.info("get callback to exit delete car process");
                editMessageText = carHandler.denyDeleteCarMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getREQUEST_DELETE_CAR_CALLBACK())) { //  callback to start delete car process
                log.info("get callback to exit delete car process");
                editMessageText = carHandler.sendCarListToDelete(incomeMessage);

            } else if (callbackData.equals(buttons.getDELETE_FIRST_CAR_CALLBACK())) { //  callback to delete first car from list
                log.info("callback to delete car first car from list");
                String deleteFirstCar = carHandler.deleteFirstCar(chatId);
                editMessageText = carHandler.deleteCarMessage(incomeMessage, deleteFirstCar);

            } else if (callbackData.equals(buttons.getDELETE_SECOND_CAR_CALLBACK())) { //  callback to delete second car from list
                log.info("callback to delete car second car from list");
                String deleteSecondCar = carHandler.deleteSecondCar(chatId);
                editMessageText = carHandler.deleteCarMessage(incomeMessage, deleteSecondCar);

            } else if (callbackData.equals(buttons.getDELETE_ALL_CARS_CALLBACK())) { //  callback to delete all cars from list
                log.info("callback to delete all cars from list");
                carHandler.deleteTwoCars(chatId);
                editMessageText = carHandler.deleteAllCarsMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getADD_CAR_EDIT_CAR_CALLBACK())) { //  callback to edit car before saving
                log.info("callback to edit car before saving");
                editMessageText = carHandler.editCarBeforeSavingStartMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getADD_CAR_EDIT_MODEL_CALLBACK())) { //  callback to edit car's model before saving
                log.info("callback to edit car's model before saving");
                editMessageText = carHandler.changeModelBeforeSavingRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getADD_CAR_EDIT_COLOR_CALLBACK())) { //  callback to edit car's color before saving
                log.info("callback to edit car's color before saving");
                editMessageText = carHandler.changeColorBeforeSavingRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getADD_CAR_EDIT_PLATES_CALLBACK())) { //  callback to edit car's plate number before saving
                log.info("callback to edit car's plate number before saving");
                editMessageText = carHandler.changePlateNumberBeforeSavingRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getADD_CAR_EDIT_COMMENTARY_CALLBACK())) { //  callback to edit car's commentary before saving
                log.info("callback to edit car's commentary before saving");
                editMessageText = carHandler.changeCommentaryBeforeSavingRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_USER_NAME_CALLBACK())) { //  callback to edit user's name
                log.info("callback to edit user's name");
                editMessageText = userHandler.editUserFirstNameMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_CAR_START_PROCESS_CALLBACK())) { //  callback to edit user's cars
                log.info("callback to edit user's cars");
                editMessageText = carHandler.sendCarListToEdit(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_CAR_CHOOSE_FIRST_CAR_CALLBACK())) { //  callback to edit first car
                log.info("callback to edit first car");
                editMessageText = carHandler.editFirstCarMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_CAR_CHOOSE_SECOND_CAR_CALLBACK())) { //  callback to edit second car
                log.info("callback to edit second car");
                editMessageText = carHandler.editSecondCarMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_FIRST_CAR_EDIT_MODEL_CALLBACK())) { //  callback to edit first car's model
                log.info("callback to edit first car's model");
                editMessageText = carHandler.changeFirstCarModelRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_SECOND_CAR_EDIT_MODEL_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit second car's model");
                editMessageText = carHandler.changeSecondCarModelRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_FIRST_CAR_EDIT_COLOR_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit first car's color");
                editMessageText = carHandler.changeFirstCarColorRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_SECOND_CAR_EDIT_COLOR_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit second car's color");
                editMessageText = carHandler.changeSecondCarColorRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_FIRST_CAR_EDIT_PLATES_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit first car's plates");
                editMessageText = carHandler.changeFirstCarPlatesRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_SECOND_CAR_EDIT_PLATES_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit second car's plates");
                editMessageText = carHandler.changeSecondCarPlatesRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_FIRST_CAR_EDIT_COMMENTARY_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit first car's commentary");
                editMessageText = carHandler.changeFirstCarCommentaryRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getEDIT_SECOND_CAR_EDIT_COMMENTARY_CALLBACK())) { //  callback to edit second car's model
                log.info("callback to edit second car's commentary");
                editMessageText = carHandler.changeSecondCarCommentaryRequestMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getDELETE_USER_START_PROCESS_CALLBACK())) { //  callback start deletion User's stored data
                log.info("callback to start deletion User's stored data");
                editMessageText = userHandler.deleteUserStartProcessMessage(incomeMessage);

            } else if (callbackData.equals(buttons.getDELETE_USER_CONFIRM_CALLBACK())) { //  callback to delete User's stored data
                log.info("callback to delete User's stored data");
                editMessageText = userHandler.deleteUserSuccessMessage(incomeMessage);
                userHandler.deleteUser(chatId);
            } else if (callbackData.startsWith(buttons.getADD_LOCATION_GET_SETTLEMENT_CALLBACK().substring(0,35))) { //  callback to delete User's stored data
                log.info("callback to choose Settlement for DepartureLocation");
                adminHandler.departureLocationSetSettlement(chatId, callbackData);
                editMessageText = adminHandler.departureLocationNameRequestMessage(incomeMessage);

            }

            sendEditMessage(editMessageText);
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

    private void adminCommandReceived(long chatId) {
        if (adminHandler.checkIsAdmin(chatId)) {
            message = adminHandler.showAdminMenuMessage(chatId);
            sendMessage(message);
        } else unknownCommandReceived(chatId);
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

    private void sendEditMessage(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error(messages.getERROR_TEXT() + e.getMessage());
        }
    }
}
