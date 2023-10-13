package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.servise.*;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

@Log4j
@Data
public class BaseHandler {
    @Autowired
    Messages messages;
    @Autowired
    Keyboards keyboards;
    @Autowired
    Buttons buttons;
    @Autowired
    UserService userService;
    @Autowired
    CarService carService;
    @Autowired
    SettlementService settlementService;
    @Autowired
    LocationService locationService;
    @Autowired
    FindPassengerRequestService findPassengerRequestService;
    @Autowired
    FindRideRequestService findRideRequestService;
    @Autowired
    ChatStatusStorageAccess chatStatusStorageAccess;
    @Autowired
    ResponseMessageProcessor messageProcessor;
    private final int PROCESS = 0;
    private final int FIRST_PARAMETER = 1;
    private final int SECOND_PARAMETER = 2;

    protected String extractParameter(String statusString, int parameterNumber) {
        String extraction = "";
        try {
            extraction = statusString.split(":")[parameterNumber];
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return extraction;
    }

    protected String extractProcess(String s) {
        return extractParameter(s, PROCESS);
    }

    protected int extractId(String s, int parameterNumber) {
        int id = -1;
        try {
            id = Integer.parseInt(extractParameter(s, parameterNumber));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return id;
    }

    protected String firstLetterToUpperCase(String s) {
        return s.replace(s.charAt(0), Character.toUpperCase(s.charAt(0)));
    }

    protected void sendBotMessage(SendMessage message) {
        messageProcessor.sendMessage(message);
    }

    protected void sendEditMessage(EditMessageText editMessage) {
        messageProcessor.sendEditedMessage(editMessage);
    }
}
