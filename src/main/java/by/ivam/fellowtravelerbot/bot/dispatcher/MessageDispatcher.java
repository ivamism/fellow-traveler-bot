package by.ivam.fellowtravelerbot.bot.dispatcher;

import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.bot.enums.BotCommands;
import by.ivam.fellowtravelerbot.bot.enums.CarOperation;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.servise.handler.*;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Data
@Log4j
public class MessageDispatcher extends Dispatcher {
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
    ChatStatusStorageAccess chatStatusStorageAccess;
    @Autowired
    FindPassengerHandler findPassengerHandler;
    @Autowired
    FindRideHandler findRideHandler;
//    @Autowired
//    ResponseMessageProcessor messageProcessor;

    SendMessage message = new SendMessage();

    private final List<String> botCommandsList =
            Arrays.stream(BotCommands.values())
                    .map(botCommands -> botCommands.getCommand())
                    .collect(Collectors.toList());

    public void onMessageReceived(Message incomeMessage) {
        if (incomeMessage.hasText()) {
            String messageText = incomeMessage.getText();
            log.info("Received message: " + messageText);
            if (hasCommand(botCommandsList, messageText)) handleBotCommand(incomeMessage);
            else handleUserMessage(incomeMessage);
        }
    }

    private boolean hasCommand(List<String> commandList, String messageText) {
        return commandList.stream().anyMatch(command -> messageText.equals(command));
    }

    private void handleBotCommand(Message incomeMessage) {
        String command = incomeMessage.getText();
        long chatId = incomeMessage.getChatId();
        log.info("Received message: " + command);
        switch (command) {
            case "/start" -> {
                startHandler.startCommandReceived(incomeMessage);
                log.info("Start chat with " + incomeMessage.getChat().getUserName() + ". ChatId: " + chatId);
            }
            case "/showMasterAdminMenu" -> {
                log.debug("get Message: " + command + " - request to send admin menu from user " + chatId);
                adminCommandReceived(chatId);
            }
            case "/help", "Помощь" -> {
                helpCommandReceived(chatId);
                log.debug("get Message: " + command);
            }
            case "/profile", "Мои данные" -> {
                if (startHandler.checkRegistration(chatId)) {
                    startHandler.noRegistrationMessage(chatId);
                } else {
                    log.debug("got request to get User's stored data");
                    userHandler.sendUserData(chatId);
                }
            }
            case "/registration" -> {
                startHandler.startMessaging(incomeMessage);
                log.debug("get Message: " + command + " - Start registration process");
            }
            case "/add_car" -> {
                if (startHandler.checkRegistration(chatId)) {
                    startHandler.noRegistrationMessage(chatId);
                } else {
                    log.debug("got request to get User's stored data");
                    carHandler.handleReceivedCallback(String.valueOf(CarOperation.ADD_CAR_REQUEST_CALLBACK), incomeMessage);
                }
            }
            case "Найти попутку" -> {
                if (startHandler.checkRegistration(chatId)) {
                    startHandler.noRegistrationMessage(chatId);
                } else {
                    log.debug("got request to find a car");
                }
            }
            case "Найти попутчика" -> {
                if (startHandler.checkRegistration(chatId)) {
                    startHandler.noRegistrationMessage(chatId);
                } else {
                    log.debug("got request to find a fellow");
                    findPassengerHandler.startCreateNewRequest(chatId);
                }
            }
            case "Добавить нас. пункт", "Добавить локацию" -> {
                log.debug("got admin command");
                if (adminHandler.checkIsAdmin(chatId)) {
                    adminHandler.handleReceivedCommand(command, incomeMessage);
                } else {
                    log.debug("user " + chatId + " not an Admin");
                    unknownCommandReceived(chatId);
                }
            }
        }
    }

    private void handleUserMessage(Message incomeMessage) {
        log.debug("method handleUserMessage");
        long chatId = incomeMessage.getChatId();
        String chatStatus = chatStatusStorageAccess.findChatStatus(chatId);
        log.debug("get chatStatus - " + chatStatus);
        if (chatStatus.contains("-")) {
            String handler = getHandler(chatStatus);
            String process = getProcess(chatStatus);
            switch (handler) {
                case "START" -> startHandler.handleReceivedMessage(process, incomeMessage);
                case "ADMIN" -> adminHandler.handleReceivedMessage(process, incomeMessage);
                case "USER" -> userHandler.handleReceivedMessage(process, incomeMessage);
                case "CAR" -> carHandler.handleReceivedMessage(process, incomeMessage);
                case "FIND_RIDE" -> findRideHandler.handleReceivedMessage(process, incomeMessage);
                case "FIND_PASSENGER" -> findPassengerHandler.handleReceivedMessage(process, incomeMessage);
                default -> unknownCommandReceived(chatId);
            }

        } else unknownCommandReceived(chatId);


//        switch (chatStatus) {
//            case "NO_STATUS" -> unknownCommandReceived(chatId);
//
////            case "REGISTRATION_USER_EDIT_NAME" -> {
////                log.info("Get edited name " + messageText);
//////                message = userHandler.confirmEditedUserFirstName(incomeMessage);
////            }
//
////            case "ADD_CAR_MODEL" -> {
////                log.info("Get model " + messageText);
////                carHandler.setModel(chatId, messageText);
////                message = carHandler.requestColor(incomeMessage);
////            }
//            case "ADD_CAR_COLOR" -> {
//                log.info("Get color " + messageText);
//                carHandler.setColor(chatId, messageText);
//                message = carHandler.requestPlateNumber(incomeMessage);
//            }
//            case "ADD_CAR_PLATES" -> {
//                log.info("Get plate number " + messageText);
//                carHandler.setPlateNumber(chatId, messageText);
//                message = carHandler.requestCommentary(incomeMessage);
//            }
//            case "ADD_CAR_COMMENTARY" -> {
//                log.info("Get commentary " + messageText);
//                carHandler.setCommentary(chatId, messageText);
//                message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
//            }
//            case "ADD_CAR_EDIT_MODEL" -> {
//                log.info("Get edited model " + messageText);
//                carHandler.setEditedBeforeSavingModel(chatId, messageText);
//                message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
//            }
//            case "ADD_CAR_EDIT_COLOR" -> {
//                log.info("Get edited color " + messageText);
//                carHandler.setEditedBeforeSavingColor(chatId, messageText);
//                message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
//            }
//            case "ADD_CAR_EDIT_PLATES" -> {
//                log.info("Get edited plates " + messageText);
//                carHandler.setEditedBeforeSavingPlateNumber(chatId, messageText);
//                message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
//            }
//            case "ADD_CAR_EDIT_COMMENTARY" -> {
//                log.info("Get edited commentary " + messageText);
//                carHandler.setEditedBeforeSavingCommentary(chatId, messageText);
//                message = carHandler.checkDataBeforeSaveCarMessage(incomeMessage);
//            }
////            case "USER_EDIT_NAME" -> {
////                log.info("Get edited User's firstname " + messageText);
////                userHandler.saveEditedUserFirstName(chatId, messageText);
////                message = userHandler.editUserFirstNameSuccessMessage(chatId);
////            }
//            case "EDIT_FIRST_CAR_MODEL" -> {
//                log.info("Get edited first car's model " + messageText);
//                Car car = carHandler.setFirstCarEditedModel(chatId, messageText);
//                message = carHandler.editionCarSuccessMessage(chatId, car);
//            }
//            case "EDIT_SECOND_CAR_MODEL" -> {
//                log.info("Get edited second car's model " + messageText);
//                Car car = carHandler.setSecondCarEditedModel(chatId, messageText);
//                message = carHandler.editionCarSuccessMessage(chatId, car);
//            }
//            case "EDIT_FIRST_CAR_COLOR" -> {
//                log.info("Get edited first car's color " + messageText);
//                Car car = carHandler.setFirstCarEditedColor(chatId, messageText);
//                message = carHandler.editionCarSuccessMessage(chatId, car);
//            }
//            case "EDIT_SECOND_CAR_COLOR" -> {
//                log.info("Get edited second car's color " + messageText);
//                Car car = carHandler.setSecondCarEditedColor(chatId, messageText);
//                message = carHandler.editionCarSuccessMessage(chatId, car);
//            }
//            case "EDIT_FIRST_CAR_PLATES" -> {
//                log.info("Get edited first car's plates " + messageText);
//                Car car = carHandler.setFirstCarEditedPlates(chatId, messageText);
//                message = carHandler.editionCarSuccessMessage(chatId, car);
//            }
//            case "EDIT_SECOND_CAR_PLATES" -> {
//                log.info("Get edited second car's plates " + messageText);
//                Car car = carHandler.setSecondCarEditedPlates(chatId, messageText);
//                message = carHandler.editionCarSuccessMessage(chatId, car);
//            }
//            case "EDIT_FIRST_CAR_COMMENTARY" -> {
//                log.info("Get edited first car's commentary " + messageText);
//                Car car = carHandler.setFirstCarEditedCommentary(chatId, messageText);
//                message = carHandler.editionCarSuccessMessage(chatId, car);
//            }
//            case "EDIT_SECOND_CAR_COMMENTARY" -> {
//                log.info("Get edited second car's commentary " + messageText);
//                Car car = carHandler.setSecondCarEditedCommentary(chatId, messageText);
//                message = carHandler.editionCarSuccessMessage(chatId, car);
//            }
//            case "ADD_SETTLEMENT_NAME" -> {
//                log.info("Get Settlement name  " + messageText);
//                Settlement settlement = adminHandler.saveSettlement(chatId, messageText);
//                message = adminHandler.settlementSaveSuccessMessage(chatId, settlement);
//            }
//            case "ADD_DEPARTURE_LOCATION_NAME" -> {
//                log.info("Get DepartureLocation name  " + messageText);
//                DepartureLocation location = adminHandler.departureLocationSave(chatId, messageText);
//                message = adminHandler.departureLocationSaveSuccessMessage(chatId, location);
//            }
//        }
//        messageProcessor.sendMessage(message);
    }

    private void unknownCommandReceived(long chatId) {
        message.setChatId(chatId);
        message.setText(messages.getUNKNOWN_COMMAND());
        sendMessage(message);
//        messageProcessor.sendMessage(message);
        log.info("received unknown command");
    }

    private void adminCommandReceived(long chatId) {
        if (adminHandler.checkIsAdmin(chatId)) {
            adminHandler.showAdminMenuMessage(chatId);
        } else unknownCommandReceived(chatId);
    }

    private void helpCommandReceived(long chatId) {
        message.setChatId(chatId);
        message.setText(messages.getHELP_TEXT());
        log.debug("method helpCommandReceived");
//        messageProcessor.sendMessage(message);
        sendMessage(message);
    }
}


