package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.redis.service.BookingService;
import by.ivam.fellowtravelerbot.servise.*;
import by.ivam.fellowtravelerbot.stateful.interfaces.ChatStatusOperations;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

@Log4j
@Data
@NoArgsConstructor
public class MessageHandler {
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
    public FindPassengerRequestService findPassengerRequestService;
    @Autowired
    public FindRideRequestService findRideRequestService;
    @Autowired
    public MatchService matchService;
    @Autowired
    public BookingService bookingService;
    @Autowired
    public RideService rideService;

    @Autowired
    public BookingTempService bookingTempService;
    @Autowired
    ChatStatusOperations chatStatusOperations;
    @Autowired
    ResponseMessageProcessor messageProcessor;
    private final int PROCESS = 0;
    private final int FIRST_VALUE = 1;
    private final int SECOND_VALUE = 2;
    private final String REGEX = ":";

    public String extractParameter(String statusString, int parameterNumber) {
        String extraction = "";
        try {
            extraction = statusString.split(REGEX)[parameterNumber];
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return extraction;
    }

    public String extractProcess(String s) {
        return extractParameter(s, PROCESS);
    }

    public int extractId(String s, int parameterNumber) {
        int id = -1;
        try {
            id = Integer.parseInt(extractParameter(s, parameterNumber));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return id;
    }

    public String firstLetterToUpperCase(String s) {
        return s.replace(s.charAt(0), Character.toUpperCase(s.charAt(0)));
    }

    protected void sendBotMessage(SendMessage message) {
        messageProcessor.sendMessage(message);
    }

    protected void sendEditMessage(EditMessageText editMessage) {
        messageProcessor.sendEditedMessage(editMessage);
    }
}
