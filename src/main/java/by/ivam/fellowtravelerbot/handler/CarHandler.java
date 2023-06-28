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
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        log.info("CarHandler method denyStart: Quit the process");

        return editMessage;
    }

    public EditMessageText denyStart(Message incomeMessage, String outgoingMessage) {
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setText(messages.getADD_CAR_DENY_START_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        log.info("CarHandler method denyStart: Quit the process");

        return editMessage;
    }


//    public EditMessageText requestVendor(Message incomeMessage) {
//        editMessage.setMessageId(incomeMessage.getMessageId());
//        editMessage.setChatId(incomeMessage.getChatId());
//        editMessage.setText(messages.getADD_CAR_ADD_VENDOR_MESSAGE());
//
//        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_VENDOR));
//
//        log.info("CarHandler method requestVendor: request to send car vendor");
//
//        return editMessage;
//    }

//    public void setVendor(Long chatId, String vendor) {
//        carDTO.setVendor(vendor.toUpperCase());
//        addCarStorageAccess.addCarDTO(chatId, carDTO);
//        log.debug("CarHandler method setVendor: set vendor " + vendor + " to carDTO and send to storage");
//    }

    public EditMessageText requestModel(Message incomeMessage) {

        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_MODEL_MESSAGE());

        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_MODEL));

        log.info("CarHandler method requestModel: request to send car model");

        return editMessage;
    }

    public void setModel(Long chatId, String model) {
        carDTO.setModel(model.toUpperCase());
        addCarStorageAccess.addCarDTO(chatId, carDTO);

        log.debug("CarHandler method setModel: set model " + model + " to carDTO and send to storage");
    }

    public SendMessage requestColor(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_COLOR_MESSAGE());
        sendMessage.setReplyMarkup(null);   //need to set null to remove no longer necessary inline keyboard

        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_COLOR));

        log.info("CarHandler method requestColor: request to send color");

        return sendMessage;
    }

    // TODO сделать первую букву всегда большой
    public void setColor(Long chatId, String color) {

        Character firstChar = color.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            color = firstCharToUpperCase(firstChar) + color.substring(1);
        }
        addCarStorageAccess.setColor(chatId, color);
        log.debug("CarHandler method setColor: set color " + color + " to carDTO and send to storage");
    }


    public SendMessage requestPlateNumber(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_PLATE_NUMBER_MESSAGE());

        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_PLATE));

        log.info("CarHandler method requestPlateNumber: request to send plate number");

        return sendMessage;
    }

    public void setPlateNumber(Long chatId, String plateNumber) {
        addCarStorageAccess.setPlateNumber(chatId, plateNumber.toUpperCase());
        log.debug("CarHandler method setPlateNumber: set plateNumber " + plateNumber + " to carDTO and send to storage");
    }

    public SendMessage requestCommentary(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getSKIP_STEP_TEXT(), buttons.getADD_CAR_SKIP_COMMENT_CALLBACK()));

        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_COMMENTARY));

        log.info("CarHandler method requestCommentary: request to send commentary");

        return sendMessage;
    }

    public void setCommentary(Long chatId, String commentary) {
        if (commentary.isEmpty()) {
            addCarStorageAccess.setCommentary(chatId, commentary);
        } else {
            Character firstChar = commentary.charAt(0);
            if (Character.isLowerCase(firstChar)) {
                commentary = firstCharToUpperCase(firstChar) + commentary.substring(1);
            }
            addCarStorageAccess.setCommentary(chatId, commentary);
        }


        log.debug("CarHandler method setCommentary: set commentary " + commentary + " to carDTO and send to storage");
    }


    public EditMessageText checkDataBeforeSaveCarMessageSkipComment(Message incomeMessage) {
        CarDTO car = addCarStorageAccess.findCarDTO(incomeMessage.getChatId());
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        String messageText = String.format(messages.getADD_CAR_CHECK_DATA_BEFORE_SAVE_MESSAGE(), car.getModel(), car.getColor(), car.getPlateNumber(), car.getCommentary());
        editMessage.setText(messageText);
        editMessage.setReplyMarkup(keyboards.threeButtonsInlineKeyboard(buttons.getSAVE_BUTTON_TEXT(), buttons.getADD_CAR_SAVE_CAR_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getADD_CAR_EDIT_CAR_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getADD_CAR_START_DENY_CALLBACK()));

        log.info("CarHandler method checkDataBeforeSaveCarMessageSkipComment: send request to check data");
        return editMessage;
    }

    public SendMessage checkDataBeforeSaveCarMessage(Message incomeMessage) {
        CarDTO car = addCarStorageAccess.findCarDTO(incomeMessage.getChatId());
        sendMessage.setChatId(incomeMessage.getChatId());
        String messageText = String.format(messages.getADD_CAR_CHECK_DATA_BEFORE_SAVE_MESSAGE(), car.getModel(), car.getColor(), car.getPlateNumber(), car.getCommentary());
        sendMessage.setText(messageText);
        sendMessage.setReplyMarkup(keyboards.threeButtonsInlineKeyboard(buttons.getSAVE_BUTTON_TEXT(), buttons.getADD_CAR_SAVE_CAR_CALLBACK(), buttons.getEDIT_BUTTON_TEXT(), buttons.getADD_CAR_EDIT_CAR_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getADD_CAR_START_DENY_CALLBACK()));

        log.info("CarHandler method checkDataBeforeSaveCarMessage: send request to check data");
        return sendMessage;
    }


    public Car saveCar(Long chatId) {
        carDTO = addCarStorageAccess.findCarDTO(chatId);
        Car car = new Car();
        car.setModel(carDTO.getModel())
                .setColor(carDTO.getColor())
                .setPlateNumber(carDTO.getPlateNumber())
                .setCommentary(carDTO.getCommentary())
                .setUser(userService.findUserById(chatId));

        carService.addNewCar(car);
        log.debug("CarHandler method addNewCar: call  carService.addNewCar to save car " + car + " to DB");
        storageAccess.deleteChatStatus(chatId);
        addCarStorageAccess.deleteCarDTO(chatId);
        return car;
    }

    public EditMessageText saveCarMessage(Message incomeMessage, Car car) {

        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        String messageText = messages.getADD_CAR_SAVE_SUCCESS_PREFIX_MESSAGE() + prepareCarToSend(car) + messages.getFURTHER_ACTION_MESSAGE();
        editMessage.setText(messageText);
        editMessage.setReplyMarkup(null);

        log.info("CarHandler method saveCarMessage: message about success add car");
        return editMessage;
    }

    private String firstCharToUpperCase(Character ch) {
        return ch.toString(Character.toUpperCase(ch));
    }

//    handling User's Cars

    private String prepareCarToSend(Car car) {
        return String.format(messages.getSHOW_CAR_MESSAGE(), car.getModel(), car.getColor(), car.getPlateNumber(), car.getCommentary());
    }

    public String prepareCarListToSend(long chatId) {
        StringBuilder text = new StringBuilder();

        for (Car car : getUsersCarsList(chatId)) {
            int n = getUsersCarsList(chatId).indexOf(car) + 1;
            text.append(n).append(prepareCarToSend(car)).append("\n");
        }
        return text.toString();
    }

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
        sendMessage.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getREQUEST_DELETE_CAR_CALLBACK(), buttons.getNO_BUTTON_TEXT(), buttons.getDENY_DELETE_CAR_CALLBACK()));
        log.info("CarHandler method startDeleteCarProcessMessageCreate: send request to confirm start of process to delete a car");
        return sendMessage;
    }

    public EditMessageText denyDeleteCarMessage(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null);   //need to set null to remove no longer necessary inline keyboard

        log.info("CarHandler method denyDeleteCarMessage: quit delete a car process");

        return editMessage;
    }

    public EditMessageText sendCarListToDelete(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(prepareCarListToSend(chatId));
        if (getUsersCarsQuantity(chatId) == 2) {
            editMessage.setReplyMarkup(keyboards.fourButtonsColumnInlineKeyboard(buttons.getFIRST_TEXT(), buttons.getDELETE_FIRST_CAR_CALLBACK(), buttons.getSECOND_TEXT(), buttons.getDELETE_SECOND_CAR_CALLBACK(), buttons.getDELETE_ALL_TEXT(), buttons.getDELETE_ALL_CARS_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getADD_CAR_START_DENY_CALLBACK()));
        } else if (getUsersCarsQuantity(chatId) == 1) {
            editMessage.setReplyMarkup(keyboards.twoButtonsColumnInlineKeyboard(buttons.getDELETE_TEXT(), buttons.getDELETE_CAR_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getDENY_DELETE_CAR_CALLBACK()));
        }

        log.info("CarHandler method sendCarListToDelete: send cars list with inline keyboard to choose a car to delete");
        return editMessage;
    }

    public String deleteFirstCar(long chatId) {
        Car car = getUsersCarsList(chatId).get(0);
        String carToSend = prepareCarToSend(car);

        log.debug("CarHandler: method deleteFirst: call CarService to delete car by Id" + car);
        carService.deleteCarById(car.getId());

        return carToSend;
    }

    public String deleteSecondCar(long chatId) {
        Car car = getUsersCarsList(chatId).get(1);
        String deletedCarToSend = prepareCarToSend(car);

        log.debug("CarHandler: method deleteSecond: call CarService to delete car by Id" + car);
        carService.deleteCarById(car.getId());

        return deletedCarToSend;
    }

    public void deleteAllCars(long chatId) {
        log.debug("CarHandler: method deleteAll: call CarService to delete car by Id" + chatId);
//        carService.deleteAllUsersCars(chatId);
        deleteSecondCar(chatId);
        deleteFirstCar(chatId);
    }

    public EditMessageText deleteCarMessage(Message incomeMessage, String deleteCarMessage) {

        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText("Автомобиль:\n" + deleteCarMessage + " удален.\n" + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.info("CarHandler method deleteCarMessage: send about deleted car");
        return editMessage;
    }

    public EditMessageText deleteAllCarsMessage(Message incomeMessage) {

        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getDELETE_ALL_CARS_MESSAGE() + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.info("CarHandler method deleteCarMessage: send about deleted car");
        return editMessage;
    }


// Edit car


}
