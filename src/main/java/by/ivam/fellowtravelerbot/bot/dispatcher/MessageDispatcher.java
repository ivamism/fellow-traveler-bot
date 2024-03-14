package by.ivam.fellowtravelerbot.bot.dispatcher;

import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.bot.enums.BotCommands;
import by.ivam.fellowtravelerbot.bot.enums.CarOperation;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.DTO.stateOperations.interfaces.ChatStatusOperations;
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
    Keyboards keyboards;
    @Autowired
    Messages messages;
    @Autowired
    ChatStatusOperations chatStatusOperations;

    SendMessage sendMessage = new SendMessage();

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
//        log.info("Received message: " + command);
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
                    userHandler.userDataToString(chatId);
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
                    findRideHandler.startCreateNewRequest(chatId);
                    log.debug("got request to findById a car");
                }
            }
            case "Найти попутчика" -> {
                if (startHandler.checkRegistration(chatId)) {
                    startHandler.noRegistrationMessage(chatId);
                } else {
                    log.debug("got request to findById a fellow");
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
        String chatStatus = chatStatusOperations.findChatStatus(chatId);
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
                case "FIND_PAS" -> findPassengerHandler.handleReceivedMessage(process, incomeMessage);
                case "MATCHING" -> matchingHandler.handleReceivedMessage(process, incomeMessage);
                default -> unknownCommandReceived(chatId);
            }

        } else unknownCommandReceived(chatId);
    }

    private void unknownCommandReceived(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getUNKNOWN_COMMAND());
        sendBotMessage(sendMessage);
        log.info("received unknown command");
    }

    private void adminCommandReceived(long chatId) {
        if (adminHandler.checkIsAdmin(chatId)) {
            adminHandler.showAdminMenuMessage(chatId);
        } else unknownCommandReceived(chatId);
    }

    private void helpCommandReceived(long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getHELP_TEXT());
        log.debug("method helpCommandReceived");
        sendBotMessage(sendMessage);
    }
}


