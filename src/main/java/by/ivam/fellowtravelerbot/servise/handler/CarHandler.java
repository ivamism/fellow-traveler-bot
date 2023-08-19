package by.ivam.fellowtravelerbot.servise.handler;

import by.ivam.fellowtravelerbot.DTO.CarDTO;
import by.ivam.fellowtravelerbot.bot.Messages;
import by.ivam.fellowtravelerbot.bot.ResponseMessageProcessor;
import by.ivam.fellowtravelerbot.bot.enums.CarOperation;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.keboards.Buttons;
import by.ivam.fellowtravelerbot.bot.keboards.Keyboards;
import by.ivam.fellowtravelerbot.model.Car;
import by.ivam.fellowtravelerbot.servise.CarService;
import by.ivam.fellowtravelerbot.servise.UserService;
import by.ivam.fellowtravelerbot.storages.ChatStatusStorageAccess;
import by.ivam.fellowtravelerbot.storages.interfaces.AddCarStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// This class handle operations with Car

@Service
@Data
@Log4j
public class CarHandler implements Handler {
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
    ChatStatusStorageAccess chatStatusStorageAccess;
    @Autowired
    UserService userService;
    @Autowired
    ResponseMessageProcessor messageProcessor;
    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        log.debug("method handleReceivedMessage");
        String messageText = incomeMessage.getText();
        Long chatId = incomeMessage.getChatId();
        log.debug("method handleReceivedMessage. get chatStatus: " + chatStatus + ". message: " + messageText);
        String process = chatStatus;
        if (chatStatus.contains(":")) {
            process = CommonMethods.trimProcess(chatStatus);
        }
        switch (process) {
            case "ADD_CAR_MODEL_CHAT_STATUS" -> {
                setModel(chatId, messageText);
                sendMessage = requestColor(incomeMessage);
            }
            case "ADD_CAR_COLOR_CHAT_STATUS" -> {
                setColor(chatId, messageText);
                sendMessage = requestPlateNumber(incomeMessage);
            }
            case "ADD_CAR_PLATES_CHAT_STATUS" -> {
                setPlateNumber(chatId, messageText);
                sendMessage = requestCommentary(incomeMessage);
            }
            case "ADD_CAR_COMMENTARY_CHAT_STATUS" -> {
                setCommentary(chatId, messageText);
                sendMessage = checkDataBeforeSaveCarMessage(incomeMessage);
            }
            case "ADD_CAR_EDIT_MODEL_CHAT_STATUS" -> {
                setEditedBeforeSavingModel(chatId, messageText);
                sendMessage = checkDataBeforeSaveCarMessage(incomeMessage);
            }
            case "ADD_CAR_EDIT_COLOR_CHAT_STATUS" -> {
                setEditedBeforeSavingColor(chatId, messageText);
                sendMessage = checkDataBeforeSaveCarMessage(incomeMessage);
            }
            case "ADD_CAR_EDIT_PLATES_CHAT_STATUS" -> {
                setEditedBeforeSavingPlateNumber(chatId, messageText);
                sendMessage = checkDataBeforeSaveCarMessage(incomeMessage);
            }
            case "ADD_CAR_EDIT_COMMENTARY_CHAT_STATUS" -> {
                setEditedBeforeSavingCommentary(chatId, messageText);
                sendMessage = checkDataBeforeSaveCarMessage(incomeMessage);
            }
            case "EDIT_CAR_MODEL_CHAT_STATUS" -> {
                Car car = setCarEditedModel(CommonMethods.trimId(chatStatus), messageText);
                sendMessage = editionCarSuccessMessage(chatId, car);
            }
            case "EDIT_CAR_COLOR_CHAT_STATUS" -> {
                Car car = setCarEditedColor(CommonMethods.trimId(chatStatus), messageText);
                sendMessage = editionCarSuccessMessage(chatId, car);
            }
            case "EDIT_CAR_PLATES_CHAT_STATUS" -> {
                Car car = setCarEditedPlates(CommonMethods.trimId(chatStatus), messageText);
                sendMessage = editionCarSuccessMessage(chatId, car);
            }
            case "EDIT_CAR_COMMENTARY_CHAT_STATUS" -> {
                Car car = setCarEditedCommentary(CommonMethods.trimId(chatStatus), messageText);
                sendMessage = editionCarSuccessMessage(chatId, car);
            }
        }
        messageProcessor.sendMessage(sendMessage);
    }

    @Override
    public void handleReceivedCallback(String callback, Message incomeMessage) {
        Long chatId = incomeMessage.getChatId();
        String process = callback;
        if (callback.contains(":")) {
            process = CommonMethods.trimProcess(callback);
        }
        log.debug("method handleReceivedCallback. get callback: " + callback);
        log.debug("process: " + process);
        switch (process) {
            case "ADD_CAR_REQUEST_CALLBACK" -> startAddCarProcess(incomeMessage);
            case "ADD_CAR_START_CALLBACK" -> editMessage = requestModel(incomeMessage);
            case "ADD_CAR_SKIP_COMMENT_CALLBACK" -> {
                setCommentary(chatId, "");
                editMessage = checkDataBeforeSaveCarMessageSkipComment(incomeMessage);
            }
            case "ADD_CAR_SAVE_CAR_CALLBACK" -> {
                Car car = saveCar(chatId);
                editMessage = saveCarMessage(incomeMessage, car);
            }
            case "ADD_CAR_EDIT_CAR_CALLBACK" -> editMessage = editCarBeforeSavingStartMessage(incomeMessage);
            case "ADD_CAR_EDIT_MODEL_CALLBACK" -> editMessage = changeModelBeforeSavingRequestMessage(incomeMessage);
            case "ADD_CAR_EDIT_COLOR_CALLBACK" -> editMessage = changeColorBeforeSavingRequestMessage(incomeMessage);
            case "ADD_CAR_EDIT_PLATES_CALLBACK" ->
                    editMessage = changePlateNumberBeforeSavingRequestMessage(incomeMessage);
            case "ADD_CAR_EDIT_COMMENTARY_CALLBACK" ->
                    editMessage = changeCommentaryBeforeSavingRequestMessage(incomeMessage);
            case "EDIT_CAR_REQUEST_CALLBACK" -> editMessage = sendCarListToEdit(incomeMessage);
            case "EDIT_CAR_CHOOSE_CAR_CALLBACK" ->
                    editMessage = editCarMessage(incomeMessage, CommonMethods.trimId(callback));
            case "EDIT_CAR_MODEL_CALLBACK" ->
                    editMessage = editCarModelRequestMessage(incomeMessage, CommonMethods.trimId(callback));
            case "EDIT_CAR_COLOR_CALLBACK" ->
                    editMessage = editCarColorRequestMessage(incomeMessage, CommonMethods.trimId(callback));
            case "EDIT_CAR_PLATES_CALLBACK" ->
                    editMessage = changeCarPlatesRequestMessage(incomeMessage, CommonMethods.trimId(callback));
            case "EDIT_CAR_COMMENTARY_CALLBACK" ->
                    editMessage = editCarCommentaryRequestMessage(incomeMessage, CommonMethods.trimId(callback));
            case "DELETE_CAR_REQUEST_CALLBACK" -> editMessage = sendCarListToDelete(incomeMessage);
            case "DELETE_CAR_CALLBACK" -> {
                String deletedCar = deleteCar(CommonMethods.trimId(callback));
                editMessage = deleteCarMessage(incomeMessage, deletedCar);
            }
            case "DELETE_ALL_CARS_CALLBACK" -> {
                deleteAllCars(chatId);
                editMessage = deleteAllCarsMessage(incomeMessage);
            }
        }
        messageProcessor.sendEditedMessage(editMessage);
    }

// add User's car step-by-step process

    public void startAddCarProcess(Message incomeMessage) {
        if (getUsersCarsQuantity(incomeMessage.getChatId()) < 2) {
            startAddCarProcessMessageCreate(incomeMessage);
        } else {
            startDeleteCarProcessMessageCreate(incomeMessage);
        }
    }

    private void startAddCarProcessMessageCreate(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_START_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.yesButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_START_CALLBACK)); // Add car button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));

        log.info("CarHandler method startAddCarProcessMessageCreate: send request to confirm start of process to add a new car");
        messageProcessor.sendMessage(sendMessage);
    }

    private EditMessageText requestModel(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getADD_CAR_ADD_MODEL_MESSAGE());
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_MODEL_CHAT_STATUS);

        log.info("CarHandler method requestModel: request to send car model");
        return editMessage;
    }

    private void setModel(Long chatId, String model) {
        carDTO.setModel(model.toUpperCase());
        addCarStorageAccess.addCarDTO(chatId, carDTO);
        log.debug("CarHandler method setModel: set model " + model + " to carDTO and send to storage");
    }

    private SendMessage requestColor(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_COLOR_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_COLOR_CHAT_STATUS);

        log.info("CarHandler method requestColor: request to send color");

        return sendMessage;
    }

    private void setColor(Long chatId, String color) {
        addCarStorageAccess.setColor(chatId, CommonMethods.firstLetterToUpperCase(color));
        log.debug("CarHandler method setColor: set color " + color + " to carDTO and send to storage");
    }

    private SendMessage requestPlateNumber(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_PLATE_NUMBER_MESSAGE());
        sendMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_PLATES_CHAT_STATUS);

        log.info("CarHandler method requestPlateNumber: request to send plate number");

        return sendMessage;
    }

    private void setPlateNumber(Long chatId, String plateNumber) {
        addCarStorageAccess.setPlateNumber(chatId, plateNumber.toUpperCase());
        log.debug("CarHandler method setPlateNumber: set plateNumber " + plateNumber + " to carDTO and send to storage");
    }

    private SendMessage requestCommentary(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE());

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.skipButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_SKIP_COMMENT_CALLBACK)); // Skip step button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));

        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_COMMENTARY_CHAT_STATUS);

        log.info("CarHandler method requestCommentary: request to send commentary");

        return sendMessage;
    }

    private void setCommentary(Long chatId, String commentary) {
        if (commentary.isEmpty()) {
            addCarStorageAccess.setCommentary(chatId, commentary);
        } else {
            addCarStorageAccess.setCommentary(chatId, CommonMethods.firstLetterToUpperCase(commentary));
        }
        log.debug("CarHandler method setCommentary: set commentary " + commentary + " to carDTO and send to storage");
    }

    private EditMessageText checkDataBeforeSaveCarMessageSkipComment(Message incomeMessage) {
        CarDTO car = addCarStorageAccess.findCarDTO(incomeMessage.getChatId());
        editMessageTextGeneralPreset(incomeMessage);

        editMessage.setText(String.format(messages.getADD_CAR_CHECK_DATA_BEFORE_SAVE_MESSAGE(), car.getModel(), car.getColor(), car.getPlateNumber(), car.getCommentary()));

        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.saveButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_SAVE_CAR_CALLBACK)); // Save button
        buttonsAttributesList.add(buttons.editButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_EDIT_CAR_CALLBACK)); // Edit button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));

        log.info("CarHandler method checkDataBeforeSaveCarMessageSkipComment: send request to check data");
        return editMessage;
    }

    private SendMessage checkDataBeforeSaveCarMessage(Message incomeMessage) {
        CarDTO car = addCarStorageAccess.findCarDTO(incomeMessage.getChatId());
        sendMessage.setChatId(incomeMessage.getChatId());
        String messageText = String.format(messages.getADD_CAR_CHECK_DATA_BEFORE_SAVE_MESSAGE(), car.getModel(), car.getColor(), car.getPlateNumber(), car.getCommentary());
        sendMessage.setText(messageText);
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.saveButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_SAVE_CAR_CALLBACK)); // Save button
        buttonsAttributesList.add(buttons.editButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_EDIT_CAR_CALLBACK)); // Edit button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));

        log.info("CarHandler method checkDataBeforeSaveCarMessage: send request to check data");
        return sendMessage;
    }

    private Car saveCar(Long chatId) {
        Car car = carService.addNewCar(addCarStorageAccess.findCarDTO(chatId), chatId);
        log.debug("CarHandler method addNewCar: call  carService.addNewCar to save car " + car + " to DB");
        chatStatusStorageAccess.deleteChatStatus(chatId);
        addCarStorageAccess.deleteCarDTO(chatId);
        return car;
    }

    private EditMessageText saveCarMessage(Message incomeMessage, Car car) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
        String messageText = messages.getADD_CAR_SAVE_SUCCESS_PREFIX_MESSAGE() + prepareCarToSend(car) + messages.getFURTHER_ACTION_MESSAGE();
        editMessage.setText(messageText);
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.info("CarHandler method saveCarMessage: message about success add car");
        return editMessage;
    }

    //         Delete cars
    private void startDeleteCarProcessMessageCreate(Message incomeMessage) {
        sendMessage.setChatId(incomeMessage.getChatId());
        sendMessage.setText(messages.getDELETE_CAR_START_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.yesButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.DELETE_CAR_REQUEST_CALLBACK)); // Delete button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
        log.info("CarHandler method startDeleteCarProcessMessageCreate: send request to confirm start of process to delete a car");
        messageProcessor.sendMessage(sendMessage);
    }

    public EditMessageText sendCarListToDelete(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        Long chatId = incomeMessage.getChatId();

        if (getUsersCarsQuantity(chatId) == 2) {
            editMessage.setText(messages.getDELETE_CAR_CHOOSE_MESSAGE() + prepareCarListToSend(chatId));

            int firstCarId = getUsersCarsList(chatId).get(0).getId();
            int secondCarId = getUsersCarsList(chatId).get(1).getId();

            List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
            buttonsAttributesList.add(buttons.firstChoiceButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.DELETE_CAR_CHOOSE_CALLBACK.getValue() + firstCarId)); // delete first car button
            buttonsAttributesList.add(buttons.secondChoiceButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.DELETE_CAR_CHOOSE_CALLBACK.getValue() + secondCarId)); // delete second car button
            buttonsAttributesList.add(buttons.deleteAllButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.DELETE_ALL_CARS_CALLBACK)); // delete both cars button
            buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
            editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));

        } else if (getUsersCarsQuantity(chatId) == 1) {
            String deletedCar = deleteCar(getUsersCarsList(chatId).get(0).getId());
            editMessage = deleteCarMessage(incomeMessage, deletedCar);
        }
        log.debug("CarHandler method sendCarListToDelete: send cars list with inline keyboard to choose a car to delete");
        return editMessage;
    }

    public String deleteCar(int carId) {
        Car car = carService.findById(carId);
        String carToSend = prepareCarToSend(car);

        log.debug("CarHandler: method deleteCar: call CarService to delete car by Id" + car);
        carService.deleteCarById(car.getId());

        return carToSend;
    }

    public void deleteAllCars(long chatId) {
        List<Integer> carIdList = getUsersCarsList(chatId)
                .stream()
                .map(car -> car.getId())
                .collect(Collectors.toList());

        log.debug("CarHandler: method deleteAll: call CarService to delete car by Id" + chatId);
        carService.deleteAllUsersCars(carIdList);
    }


    public EditMessageText deleteCarMessage(Message incomeMessage, String deleteCarMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText("Автомобиль:\n" + deleteCarMessage + " удален.\n" + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.info("CarHandler method deleteCarMessage: send message about deleted car");
        return editMessage;
    }

    public EditMessageText deleteAllCarsMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);

        editMessage.setText(messages.getDELETE_ALL_CARS_MESSAGE() + messages.getFURTHER_ACTION_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        log.debug("CarHandler method deleteCarMessage: send message about cars deletion");
        return editMessage;
    }

    // Edit car
    private EditMessageText editCarBeforeSavingStartMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getEDIT_CAR_START_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.modelButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_EDIT_MODEL_CALLBACK)); // Edit model button
        buttonsAttributesList.add(buttons.colorButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_EDIT_COLOR_CALLBACK)); // Edit color button
        buttonsAttributesList.add(buttons.platesButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_EDIT_PLATES_CALLBACK)); // Edit plates button
        buttonsAttributesList.add(buttons.commentaryButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_EDIT_COMMENTARY_CALLBACK)); // Edit commentary button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));

        log.debug("CarHandler method editCarBeforeSavingStartMessage: send message about cars edition");
        return editMessage;
    }

    private EditMessageText changeModelBeforeSavingRequestMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getADD_CAR_ADD_MODEL_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_EDIT_MODEL_CHAT_STATUS);

        log.info("CarHandler method changeModelRequestMessage: request to send car model");
        return editMessage;
    }

    private void setEditedBeforeSavingModel(Long chatId, String model) {
        addCarStorageAccess.setModel(chatId, model.toUpperCase());
        chatStatusStorageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method setEditedModel: set model " + model + " to carDTO and send to storage");
    }

    private EditMessageText changeColorBeforeSavingRequestMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getADD_CAR_ADD_COLOR_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard

        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_EDIT_COLOR_CHAT_STATUS);
        log.info("CarHandler method changeColorRequestMessage: request to send color");

        return editMessage;
    }

    private void setEditedBeforeSavingColor(Long chatId, String color) {
        addCarStorageAccess.setColor(chatId, CommonMethods.firstLetterToUpperCase(color));
        chatStatusStorageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method setEditedModel: set model " + color + " to carDTO and send to storage");
    }

    private EditMessageText changePlateNumberBeforeSavingRequestMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getADD_CAR_ADD_PLATE_NUMBER_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_EDIT_PLATES_CHAT_STATUS);

        log.info("CarHandler method changePlateNumberRequestMessage: request to send plate number");

        return editMessage;
    }

    private void setEditedBeforeSavingPlateNumber(Long chatId, String plateNumber) {

        addCarStorageAccess.setPlateNumber(chatId, plateNumber.toUpperCase());
        chatStatusStorageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method setEditedPlateNumber: set model " + plateNumber + " to carDTO and send to storage");
    }

    private EditMessageText changeCommentaryBeforeSavingRequestMessage(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        editMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE());
        editMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_EDIT_COMMENTARY_CHAT_STATUS);

        log.info("CarHandler method changeCommentaryRequestMessage: request to send commentary");
        return editMessage;
    }

    private void setEditedBeforeSavingCommentary(Long chatId, String commentary) {
        addCarStorageAccess.setCommentary(chatId, CommonMethods.firstLetterToUpperCase(commentary));
        chatStatusStorageAccess.deleteChatStatus(chatId);
        log.debug("CarHandler method setEditedCommentary: set commentary " + commentary + " to carDTO and send to storage");
    }

    private EditMessageText sendCarListToEdit(Message incomeMessage) {
        editMessageTextGeneralPreset(incomeMessage);
        Long chatId = incomeMessage.getChatId();
        if (getUsersCarsQuantity(chatId) == 2) {
            int firstCarId = getUsersCarsList(chatId).get(0).getId();
            int secondCarId = getUsersCarsList(chatId).get(1).getId();
            editMessage.setText(prepareCarListToSend(chatId));

            List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
            buttonsAttributesList.add(buttons.firstChoiceButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_CHOOSE_CAR_CALLBACK.getValue() + firstCarId)); // Choose first car button
            buttonsAttributesList.add(buttons.secondChoiceButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_CHOOSE_CAR_CALLBACK.getValue() + secondCarId)); // Choose second car button
            buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
            editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));
        } else if (getUsersCarsQuantity(chatId) == 1) {
            editCarMessage(incomeMessage, getUsersCarsList(chatId).get(0).getId());
        }
        log.debug("CarHandler method sendCarListToEdit: send cars list with inline keyboard to choose a car to edit");
        return editMessage;
    }

    private EditMessageText editCarMessage(Message incomeMessage, int carId) {
        Car car = carService.findById(carId);
        String carToSend = prepareCarToSend(car);
        editMessageTextGeneralPreset(incomeMessage);

        editMessage.setText(String.format(messages.getEDIT_CAR_CHOSEN_PREFIX_MESSAGE(), carToSend) + messages.getEDIT_CAR_START_MESSAGE());
        List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
        buttonsAttributesList.add(buttons.modelButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_MODEL_CALLBACK.getValue() + carId)); // Edit model button
        buttonsAttributesList.add(buttons.colorButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_COLOR_CALLBACK.getValue() + carId)); // Edit color button
        buttonsAttributesList.add(buttons.platesButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_PLATES_CALLBACK.getValue() + carId)); // Edit plates button
        buttonsAttributesList.add(buttons.commentaryButtonCreate(Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_COMMENTARY_CALLBACK.getValue() + carId)); // Edit commentary button
        buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
        editMessage.setReplyMarkup(keyboards.dynamicRangeColumnInlineKeyboard(buttonsAttributesList));

        log.debug("CarHandler: method editFirstCarMessage: send request what User wants to edit");
        return editMessage;
    }

    private EditMessageText editCarModelRequestMessage(Message incomeMessage, int carId) {
        editMessageTextGeneralPreset(incomeMessage);
        String modelActualValue = carService.findById(carId).getModel();

        editMessage.setText(messages.getADD_CAR_ADD_MODEL_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), modelActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_MODEL_CHAT_STATUS.getValue() + carId);
        log.info("CarHandler method editCarModelRequestMessage: request to send new value of first car model");

        return editMessage;
    }

    private Car setCarEditedModel(int carId, String model) {
        log.debug("CarHandler method setCarEditedModel: set new value of model " + model);
        return carService.findById(carId).setModel(model.toUpperCase());
    }

    private EditMessageText editCarColorRequestMessage(Message incomeMessage, int carId) {
        editMessageTextGeneralPreset(incomeMessage);
        String colorActualValue = carService.findById(carId).getColor();

        editMessage.setText(messages.getADD_CAR_ADD_COLOR_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), colorActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_COLOR_CHAT_STATUS.getValue() + carId);
        log.info("CarHandler method editCarColorRequestMessage: request to send new value of first car color");

        return editMessage;
    }

    private Car setCarEditedColor(int carId, String color) {
        log.debug("CarHandler method setCarEditedColor: set new value of color " + color);
        return carService.findById(carId).setColor(CommonMethods.firstLetterToUpperCase(color));
    }

    private EditMessageText changeCarPlatesRequestMessage(Message incomeMessage, int carId) {
        editMessageTextGeneralPreset(incomeMessage);
        String platesActualValue = carService.findById(carId).getPlateNumber();
        editMessage.setText(messages.getADD_CAR_ADD_PLATE_NUMBER_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), platesActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_PLATES_CHAT_STATUS.getValue() + carId);
        log.info("CarHandler method changeCarPlatesRequestMessage: request to send new value of first car plates number");

        return editMessage;
    }

    private Car setCarEditedPlates(int carId, String plates) {
        log.debug("CarHandler method setCarEditedPlates: set new value of plates " + plates);
        return carService.findById(carId).setPlateNumber(plates.toUpperCase());
    }

    private EditMessageText editCarCommentaryRequestMessage(Message incomeMessage, int carId) {
        editMessageTextGeneralPreset(incomeMessage);
        String commentaryActualValue = carService.findById(carId).getCommentary();

        editMessage.setText(messages.getADD_CAR_ADD_COMMENTARY_MESSAGE() + String.format(messages.getACTUAL_VALUE_MESSAGE(), commentaryActualValue));
        editMessage.setReplyMarkup(keyboards.oneButtonsInlineKeyboard(buttons.cancelButtonCreate()));
        chatStatusStorageAccess.addChatStatus(incomeMessage.getChatId(), Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_COMMENTARY_CHAT_STATUS.getValue() + carId);
        log.info("CarHandler method editCarCommentaryRequestMessage: request to send new value of first car plates number");

        return editMessage;
    }

    private Car setCarEditedCommentary(int carId, String commentary) {
        log.debug("CarHandler method setCarEditedCommentary: set new value of plates " + commentary);
        return carService.findById(carId).setCommentary(CommonMethods.firstLetterToUpperCase(commentary));
    }

    public SendMessage editionCarSuccessMessage(long chatId, Car car) {
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.getEDIT_CAR_SUCCESS_PREFIX_MESSAGE() + prepareCarToSend(car) + messages.getFURTHER_ACTION_MESSAGE());
        sendMessage.setReplyMarkup(null); //need to set null to remove no longer necessary inline keyboard
        return sendMessage;
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
    public int getUsersCarsQuantity(long chatId) {
        return getUsersCarsList(chatId).size();
    }
    public void editMessageTextGeneralPreset(Message incomeMessage) {
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
    }
}
