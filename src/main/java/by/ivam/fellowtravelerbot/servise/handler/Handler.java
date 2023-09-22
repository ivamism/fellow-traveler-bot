package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.servise.*;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.time.LocalTime;

@Log4j
public class Handler {
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

    public String trimProcess(String s) {
        String subS = " ";
        try {
            subS = s.split(":")[0];
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return subS;
    }


    public String trimSecondSubstring(String s) {
        String subS = " ";
        try {
            subS = s.split(":")[1];
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return subS;
    }

    public int trimId(String s) {
        int id = -1;
        try {
          String  subS = s.split(":")[1];
           id= Integer.parseInt(subS);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return id;
    }

    public int trimSecondId(String s) {
        String[] strings = new String[0];
        int id = -1;
        try {
            strings = s.split(":");
            if (strings.length > 2) {
                id = Integer.parseInt(strings[2]);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return id;
    }

    public String firstLetterToUpperCase(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public void sendBotMessage(SendMessage message) {
        messageProcessor.sendMessage(message);
    }

    public void sendEditMessage(EditMessageText editMessage) {
        messageProcessor.sendEditedMessage(editMessage);
    }
}
