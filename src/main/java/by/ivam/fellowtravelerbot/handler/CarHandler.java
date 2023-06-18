package by.ivam.fellowtravelerbot.handler;

import by.ivam.fellowtravelerbot.DTO.CarDTO;
import by.ivam.fellowtravelerbot.bot.Buttons;
import by.ivam.fellowtravelerbot.bot.Keyboards;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.handler.enums.ChatStatus;
import by.ivam.fellowtravelerbot.model.Car;
import by.ivam.fellowtravelerbot.servise.CarService;
import by.ivam.fellowtravelerbot.servise.UserService;
import by.ivam.fellowtravelerbot.storages.AddCarStorageAccess;
import by.ivam.fellowtravelerbot.storages.StorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

// This class handle operations with Car

@Component
@Data
@Log4j
public class CarHandler {
    @Autowired
    CarDTO carDTO;
    @Autowired
    Messages messages;
    @Autowired
    CarService carService;
    @Autowired
    Keyboards keyboards;
    @Autowired
    Buttons buttons;
    @Autowired
    AddCarStorageAccess addCarStorageAccess;
    @Autowired
    StorageAccess storageAccess;
    @Autowired
    UserService userService;
    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();

// add users car step-by-step process

    public SendMessage startAddCarProcess(Message incomeMessage) {
        long chatId = incomeMessage.getChatId();
        if (getUsersCarsQuantity(chatId) < 2) {
            sendMessage = startAddCarProcessMessageCreate(incomeMessage);
        } else {
            sendMessage = startDeleteCarProcessMessageCreate(incomeMessage);
        }
        return sendMessage;
    }

    private SendMessage startAddCarProcessMessageCreate(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_START_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getADD_CAR_START_CALLBACK(), buttons.getNO_BUTTON_TEXT(), buttons.getADD_CAR_START_DENY_CALLBACK()));
        log.info("CarHandler method start: send request to confirm start of process to add a new car");
        return sendMessage;
    }


    public EditMessageText denyStart(Message incomeMessage) {
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setText(messages.getADD_CAR_DENY_START_MESSAGE());
        log.info("CarHandler method denyStart: Quit add car process");

        return editMessage;
    }

    public EditMessageText requestVendor(Message incomeMessage) {
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setText(messages.getADD_CAR_ADD_VENDOR_MESSAGE());

        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_VENDOR));

        log.info("CarHandler method requestVendor: request to send car vendor");

        return editMessage;
    }

    public void setVendor(Long chatId, String vendor) {
        carDTO.setVendor(vendor);
        addCarStorageAccess.addCarDTO(chatId, carDTO);
        log.debug("CarHandler method setVendor: set vendor " + vendor + " to carDTO and send to storage");
    }

    public SendMessage requestModel(Message incomeMessage) {

        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_MODEL_MESSAGE());
        sendMessage.setReplyMarkup(null);

        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_MODEL));

        log.info("CarHandler method requestModel: request to send car model");

        return sendMessage;
    }

    public void setModel(Long chatId, String model) {
        addCarStorageAccess.setModel(chatId, model);
        log.debug("CarHandler method setModel: set model " + model + " to carDTO and send to storage");
    }

    public SendMessage requestColor(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_COLOR_MESSAGE());

        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_COLOR));

        log.info("CarHandler method requestColor: request to send color");

        return sendMessage;
    }

    public void setColor(Long chatId, String color) {
        addCarStorageAccess.setColor(chatId, color);
        log.debug("CarHandler method setColor: set color " + color + " to carDTO and send to storage");
    }

    public SendMessage requestPlateNumber(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_PLATE_NUMBER_MESSAGE());

        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_PLATE));

        log.info("CarHandler method requestPlateNumber: request to send color");

        return sendMessage;
    }

    public void setPlateNumber(Long chatId, String plateNumber) {
        addCarStorageAccess.setPlateNumber(chatId, plateNumber);
        log.debug("CarHandler method setPlateNumber: set plateNumber " + plateNumber + " to carDTO and send to storage");
    }
//    TODO добавить кнопку отказа от коментариев
    public SendMessage requestCommentary(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getNO_COMMENT_TEXT(), buttons.getADD_CAR_NO_COMMENT_CALLBACK()));

        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_COMMENTARY));

        log.info("CarHandler method requestPlateNumber: request to send color");

        return sendMessage;
    }

    public void setCommentary(Long chatId, String commentary) {
        addCarStorageAccess.setCommentary(chatId, commentary);
        log.debug("CarHandler method setCommentary: set commentary " + commentary + " to carDTO and send to storage");
    }

    // TODO добавить инлайн клавиатуру для подтверждения и редактирования данных
    public Car saveCar(Long chatId) {
        carDTO = addCarStorageAccess.findCarDTO(chatId);
        Car car = new Car();
        car.setVendor(carDTO.getVendor().toUpperCase())
                .setModel(carDTO.getModel().toUpperCase())
                .setColor(carDTO.getColor())
                .setPlateNumber(carDTO.getPlateNumber().toUpperCase())
                .setCommentary(carDTO.getCommentary())
                .setUser(userService.findUserById(chatId));

        carService.addNewCar(car);
        log.debug("CarHandler method addNewCar: call  carService.addNewCar to save car " + car + " to DB");
        storageAccess.deleteChatStatus(chatId);
        addCarStorageAccess.deleteCarDTO(chatId);
        return car;
    }

    public SendMessage saveCarMessage(Message incomeMessage, Car car) {
        sendMessage.setChatId(incomeMessage.getChatId());
        String messageText = String.format(messages.getADD_CAR_ADD_SUCCESS_MESSAGE(), car.getVendor(), car.getModel(), car.getColor(), car.getPlateNumber(), car.getCommentary());
        sendMessage.setText(messageText);

        log.info("CarHandler method saveCarMessage: message about success add car");
        return sendMessage;
    }
//    handling Users Cars quantity

    private List<Car> getUsersCarsList(long chatId) {
        return carService.usersCarList(chatId);
    }

    private int getUsersCarsQuantity(long chatId) {
        return getUsersCarsList(chatId).size();
    }

//         Delete cars
private SendMessage startDeleteCarProcessMessageCreate(Message incomeMessage) {
    sendMessage.setChatId(incomeMessage.getChatId());
    sendMessage.setText(messages.getDELETE_CAR_START_MESSAGE());
//    sendMessage.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getADD_CAR_START(), buttons.getNO_BUTTON_TEXT(), buttons.getADD_CAR_START_DENY()));
    log.info("CarHandler method startDeleteCarProcessMessageCreate: send request to confirm start of process to delete a car");
    return sendMessage;
}


}
