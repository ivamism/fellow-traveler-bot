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
import java.util.Optional;

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

    public EditMessageText quitProcessMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setChatId(chatId);
        editMessage.setText(messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        log.info("CarHandler method quitProcessMessage: Quit the process");
        storageAccess.deleteChatStatus(chatId);
        return editMessage;
    }

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

    public void setColor(Long chatId, String color) {

        addCarStorageAccess.setColor(chatId, firstLetterToUpperCase(color));
        log.debug("CarHandler method setColor: set color " + color + " to carDTO and send to storage");
    }


    public SendMessage requestPlateNumber(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_PLATE_NUMBER_MESSAGE());

        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_PLATES));

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
            addCarStorageAccess.setCommentary(chatId, firstLetterToUpperCase(commentary));
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

// TODO вынести строки 189 - 194 в метод addNewCar, в качестве параметра передать туда CarDTO
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
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.info("CarHandler method saveCarMessage: message about success add car");
        return editMessage;
    }


    //         Delete cars
    private SendMessage startDeleteCarProcessMessageCreate(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getDELETE_CAR_START_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.twoButtonsInlineKeyboard(buttons.getYES_BUTTON_TEXT(), buttons.getREQUEST_DELETE_CAR_CALLBACK(), buttons.getNO_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        log.info("CarHandler method startDeleteCarProcessMessageCreate: send request to confirm start of process to delete a car");
        return sendMessage;
    }

    public EditMessageText denyDeleteCarMessage(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null);   //need to set null to remove no longer necessary inline keyboard

        log.debug("CarHandler method denyDeleteCarMessage: quit delete a car process");

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
            editMessage.setReplyMarkup(keyboards.twoButtonsColumnInlineKeyboard(buttons.getDELETE_TEXT(), buttons.getDELETE_CAR_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        }

        log.debug("CarHandler method sendCarListToDelete: send cars list with inline keyboard to choose a car to delete");
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

    public void deleteTwoCars(long chatId) {
//        Todo переделать на удаление через отправку массива карайди
        log.debug("CarHandler: method deleteAll: call CarService to delete car by Id" + chatId);
//        carService.deleteAllUsersCars(chatId);
        deleteSecondCar(chatId);
        deleteFirstCar(chatId);
    }
    public void deleteAllCars(long chatId){
        switch (getUsersCarsQuantity(chatId)){
            case 1 -> deleteFirstCar(chatId);
            case 2 -> deleteTwoCars(chatId);
        }
    }

    public EditMessageText deleteCarMessage(Message incomeMessage, String deleteCarMessage) {

        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText("Автомобиль:\n" + deleteCarMessage + " удален.\n" + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.info("CarHandler method deleteCarMessage: send message about deleted car");
        return editMessage;
    }

    public EditMessageText deleteAllCarsMessage(Message incomeMessage) {

        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getDELETE_ALL_CARS_MESSAGE() + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.debug("CarHandler method deleteCarMessage: send message about cars deletion");
        return editMessage;
    }


    // Edit car
    public EditMessageText editCarBeforeSavingStartMessage(Message incomeMessage) {

        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getEDIT_CAR_START_MESSAGE());
        editMessage.setReplyMarkup(keyboards.fiveButtonsColumnInlineKeyboard(buttons.getMODEL_TEXT(), buttons.getADD_CAR_EDIT_MODEL_CALLBACK(), buttons.getCOLOR_TEXT(), buttons.getADD_CAR_EDIT_COLOR_CALLBACK(), buttons.getPLATES_TEXT(), buttons.getADD_CAR_EDIT_PLATES_CALLBACK(), buttons.getCOMMENTARY_TEXT(), buttons.getADD_CAR_EDIT_COMMENTARY_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));

        log.debug("CarHandler method editCarBeforeSavingStartMessage: send message about cars edition");
        return editMessage;
    }

    public EditMessageText changeModelBeforeSavingRequestMessage(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_MODEL_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_EDIT_MODEL));
        log.info("CarHandler method changeModelRequestMessage: request to send car model");

        return editMessage;
    }

    public void setEditedBeforeSavingModel(Long chatId, String model) {

        addCarStorageAccess.setModel(chatId, model.toUpperCase());
        storageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method setEditedModel: set model " + model + " to carDTO and send to storage");
    }

    public EditMessageText changeColorBeforeSavingRequestMessage(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_COLOR_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_EDIT_COLOR));
        log.info("CarHandler method changeColorRequestMessage: request to send color");

        return editMessage;
    }

    public void setEditedBeforeSavingColor(Long chatId, String color) {
        addCarStorageAccess.setColor(chatId, firstLetterToUpperCase(color));
        storageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method setEditedModel: set model " + color + " to carDTO and send to storage");
    }

    public EditMessageText changePlateNumberBeforeSavingRequestMessage(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_PLATE_NUMBER_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_EDIT_PLATES));

        log.info("CarHandler method changePlateNumberRequestMessage: request to send plate number");

        return editMessage;
    }

    public void setEditedBeforeSavingPlateNumber(Long chatId, String plateNumber) {

        addCarStorageAccess.setPlateNumber(chatId, plateNumber.toUpperCase());
        storageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method setEditedPlateNumber: set model " + plateNumber + " to carDTO and send to storage");
    }

    public EditMessageText changeCommentaryBeforeSavingRequestMessage(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.ADD_CAR_EDIT_COMMENTARY));
        log.info("CarHandler method changeCommentaryRequestMessage: request to send commentary");

        return editMessage;
    }

    public void setEditedBeforeSavingCommentary(Long chatId, String commentary) {

        addCarStorageAccess.setCommentary(chatId, firstLetterToUpperCase(commentary));
        storageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method setEditedCommentary: set commentary " + commentary + " to carDTO and send to storage");
    }

    public EditMessageText sendCarListToEdit(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(prepareCarListToSend(chatId));
        if (getUsersCarsQuantity(chatId) == 2) {
            editMessage.setReplyMarkup(keyboards.treeButtonsColumnInlineKeyboard(buttons.getFIRST_TEXT(), buttons.getEDIT_CAR_CHOOSE_FIRST_CAR_CALLBACK(), buttons.getSECOND_TEXT(), buttons.getEDIT_CAR_CHOOSE_SECOND_CAR_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getADD_CAR_START_DENY_CALLBACK()));
        } else if (getUsersCarsQuantity(chatId) == 1) {
            editMessage.setReplyMarkup(keyboards.twoButtonsColumnInlineKeyboard(buttons.getEDIT_BUTTON_TEXT(), buttons.getEDIT_CAR_CHOOSE_FIRST_CAR_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        }

        log.debug("CarHandler method sendCarListToEdit: send cars list with inline keyboard to choose a car to edit");
        return editMessage;
    }

    public EditMessageText editFirstCarMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        Car car = getUsersCarsList(chatId).get(0);
        String carToSend = prepareCarToSend(car);
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(String.format(messages.getEDIT_CAR_CHOSEN_PREFIX_MESSAGE(), carToSend) + messages.getEDIT_CAR_START_MESSAGE());
        editMessage.setReplyMarkup(keyboards.fiveButtonsColumnInlineKeyboard(buttons.getMODEL_TEXT(), buttons.getEDIT_FIRST_CAR_EDIT_MODEL_CALLBACK(), buttons.getCOLOR_TEXT(), buttons.getEDIT_FIRST_CAR_EDIT_COLOR_CALLBACK(), buttons.getPLATES_TEXT(), buttons.getEDIT_FIRST_CAR_EDIT_PLATES_CALLBACK(), buttons.getCOMMENTARY_TEXT(), buttons.getEDIT_FIRST_CAR_EDIT_COMMENTARY_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));


        log.debug("CarHandler: method editFirstCarMessage: send request what User wants to edit");


        return editMessage;
    }

    public EditMessageText editSecondCarMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        Car car = getUsersCarsList(chatId).get(1);
        String carToSend = prepareCarToSend(car);
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(String.format(messages.getEDIT_CAR_CHOSEN_PREFIX_MESSAGE(), carToSend) + messages.getEDIT_CAR_START_MESSAGE());
        editMessage.setReplyMarkup(keyboards.fiveButtonsColumnInlineKeyboard(buttons.getMODEL_TEXT(), buttons.getEDIT_SECOND_CAR_EDIT_MODEL_CALLBACK(), buttons.getCOLOR_TEXT(), buttons.getEDIT_SECOND_CAR_EDIT_COLOR_CALLBACK(), buttons.getPLATES_TEXT(), buttons.getEDIT_SECOND_CAR_EDIT_PLATES_CALLBACK(), buttons.getCOMMENTARY_TEXT(), buttons.getEDIT_SECOND_CAR_EDIT_COMMENTARY_CALLBACK(), buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));

        log.debug("CarHandler: method editSecondCarMessage: send request what User wants to edit");

        return editMessage;
    }

    public EditMessageText changeFirstCarModelRequestMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String modelActualValue = getFirstCar(chatId).getModel();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_MODEL_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), modelActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.EDIT_FIRST_CAR_MODEL));
        log.info("CarHandler method changeFirstCarModelRequestMessage: request to send new value of first car model");

        return editMessage;
    }

    public Car setFirstCarEditedModel(Long chatId, String model) {
        log.debug("CarHandler method setFirstCarEditedModel: set new value of model " + model);
        Car car = getFirstCar(chatId);
        car.setModel(model.toUpperCase());

        return saveEditedCar(chatId, car);
    }
    public EditMessageText changeFirstCarColorRequestMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String colorActualValue = getFirstCar(chatId).getColor();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_COLOR_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), colorActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.EDIT_FIRST_CAR_COLOR));
        log.info("CarHandler method changeFirstCarColorRequestMessage: request to send new value of first car color");

        return editMessage;
    }

    public Car setFirstCarEditedColor(Long chatId, String color) {
        log.debug("CarHandler method setFirstCarEditedModel: set new value of color " + color);
        Car car = getFirstCar(chatId);
        car.setColor(firstLetterToUpperCase(color));
        return saveEditedCar(chatId, car);
    }
    public EditMessageText changeFirstCarPlatesRequestMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String platesActualValue = getFirstCar(chatId).getPlateNumber();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_PLATE_NUMBER_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), platesActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.EDIT_FIRST_CAR_PLATES));
        log.info("CarHandler method changeFirstCarPlatesRequestMessage: request to send new value of first car plates number");

        return editMessage;
    }

    public Car setFirstCarEditedPlates(Long chatId, String plates) {
        log.debug("CarHandler method setFirstCarEditedModel: set new value of plates " + plates);
        Car car = getFirstCar(chatId);
        car.setPlateNumber(plates.toUpperCase());

        return saveEditedCar(chatId, car);
    }
    public EditMessageText changeFirstCarCommentaryRequestMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String commentaryActualValue = getFirstCar(chatId).getCommentary();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), commentaryActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.EDIT_FIRST_CAR_COMMENTARY));
        log.info("CarHandler method changeFirstCarCommentaryRequestMessage: request to send new value of first car commentary");

        return editMessage;
    }

    public Car setFirstCarEditedCommentary(Long chatId, String commentary) {
        log.debug("CarHandler method setFirstCarEditedCommentary: set new value of commentary " + commentary);
        Car car = getFirstCar(chatId);
        car.setCommentary(firstLetterToUpperCase(commentary));

        return saveEditedCar(chatId, car);
    }
    public EditMessageText changeSecondCarModelRequestMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String modelActualValue = getSecondCar(chatId).getModel();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_MODEL_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), modelActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.EDIT_SECOND_CAR_MODEL));
        log.info("CarHandler method changeFirstCarModelRequestMessage: request to send new value of second car model");

        return editMessage;
    }

    public Car setSecondCarEditedModel(Long chatId, String model) {
        log.debug("CarHandler method setSecondCarEditedModel: set new value of model " + model);
        Car car = getSecondCar(chatId);
        car.setModel(model.toUpperCase());

        return saveEditedCar(chatId, car);
    }
    public EditMessageText changeSecondCarColorRequestMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String colorActualValue = getSecondCar(chatId).getColor();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_COLOR_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), colorActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.EDIT_SECOND_CAR_COLOR));
        log.info("CarHandler method changeSecondCarColorRequestMessage: request to send new value of second car color");

        return editMessage;
    }

    public Car setSecondCarEditedColor(Long chatId, String color) {
        log.debug("CarHandler method setSecondCarEditedColor: set new value of color " + color);
        Car car = getSecondCar(chatId);
        car.setColor(firstLetterToUpperCase(color));
        return saveEditedCar(chatId, car);
    }
    public EditMessageText changeSecondCarPlatesRequestMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String platesActualValue = getSecondCar(chatId).getPlateNumber();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_PLATE_NUMBER_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), platesActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.EDIT_SECOND_CAR_PLATES));
        log.info("CarHandler method changeSecondCarPlatesRequestMessage: request to send new value of second car plates number");

        return editMessage;
    }

    public Car setSecondCarEditedPlates(Long chatId, String plates) {
        log.debug("CarHandler method setSecondCarEditedPlates: set new value of plates " + plates);
        Car car = getSecondCar(chatId);
        car.setPlateNumber(plates.toUpperCase());

        return saveEditedCar(chatId, car);
    }
    public EditMessageText changeSecondCarCommentaryRequestMessage(Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String commentaryActualValue = Optional.ofNullable(getSecondCar(chatId).getCommentary()).orElse("Нет комментария") ;
        editMessage.setChatId(chatId);
        editMessage.setMessageId(incomeMessage.getMessageId());
        editMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), commentaryActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));
        storageAccess.addChatStatus(incomeMessage.getChatId(), String.valueOf(ChatStatus.EDIT_SECOND_CAR_COMMENTARY));
        log.info("CarHandler method changeSecondCarCommentaryRequestMessage: request to send new value of second car commentary");

        return editMessage;
    }
    public Car setSecondCarEditedCommentary(Long chatId, String commentary) {
        log.debug("CarHandler method setSecondCarEditedCommentary: set new value of commentary " + commentary);
        Car car = getSecondCar(chatId);
        car.setCommentary(firstLetterToUpperCase(commentary));

        return saveEditedCar(chatId, car);
    }
    public SendMessage editionCarSuccessMessage(long chatId, Car car) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getEDIT_CAR_SUCCESS_PREFIX_MESSAGE() + prepareCarToSend(car) + messages.getFURTHER_ACTION_MESSAGE());
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        return sendMessage;
    }
    private Car saveEditedCar(long chatId, Car car) {
        log.debug("CarHandler method editCar: update car " + car);
        storageAccess.deleteChatStatus(chatId);
        return carService.updateCar(car);
    }

    private String firstLetterToUpperCase(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
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

    private Car getFirstCar(long chatId) {
        return getUsersCarsList(chatId).get(0);
    }

    private Car getSecondCar(long chatId) {
        return getUsersCarsList(chatId).get(1);
    }
}
