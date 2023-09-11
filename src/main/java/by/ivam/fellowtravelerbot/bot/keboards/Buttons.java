package by.ivam.fellowtravelerbot.bot.keboards;
/*
This class contains Strings of buttons names and callback-queries which used in keyboards
and methods od creation of buttons attributes pairs (name and callback data)
 */

import by.ivam.fellowtravelerbot.bot.enums.CarOperation;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.bot.enums.Operation;
import by.ivam.fellowtravelerbot.bot.enums.UserOperation;
import lombok.Getter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Getter
@Component
public class Buttons {

    // Buttons Names

    private final String YES_BUTTON_TEXT = "Да";
    private final String NO_BUTTON_TEXT = "Нет";
    private final String SAVE_BUTTON_TEXT = "Сохранить";
    private final String EDIT_BUTTON_TEXT = "Редактировать";
    private final String CANCEL_BUTTON_TEXT = "Отменить";
    private final String SEND_BUTTON_TEXT = "Отправить";
    private final String SKIP_STEP_TEXT = "Пропустить";
    private final String ANOTHER_TEXT = "Другой";
    private final String DELETE_TEXT = "Удалить";
    private final String DELETE_ALL_TEXT = "Удалить всё";
    private final String FIRST_TEXT = String.valueOf(1);
    private final String SECOND_TEXT = String.valueOf(2);
    private final String MODEL_TEXT = "Модель";
    private final String COLOR_TEXT = "Цвет";
    private final String PLATES_TEXT = "Регистрационный номер";
    private final String COMMENTARY_TEXT = "Комментарий";
    private final String CHANGE_NAME_TEXT = "Изменить имя";
    private final String CHANGE_RESIDENCE_TEXT = "Изменить место жительства";
    private final String ADD_CAR_TEXT = "Добавить автомобиль";
    private final String EDIT_CAR_TEXT = "Изменить автомобиль";
    private final String DELETE_CAR_TEXT = "Удалить автомобиль";
    private final String TOWARD_MINSK_TEXT = "В Минск";
    private final String FROM_MINSK_TEXT = "Из Минска";
    private final String TODAY_TEXT = "Сегодня";
    private final String TOMORROW_TEXT = "Завтра";
    private final String SETTLEMENT_LOCATION_TEXT = "Нас.пункт/место";
    private final String DATE_TIME_TEXT = "Дата/время";
    private final String CAR_DETAILS_TEXT = "Данные автомобиля";
    private final String DEPARTURE_SETTLEMENT_TEXT = "Нас. пункт выезда";
    private final String DEPARTURE_LOCATION_TEXT = "Место выезда";
    private final String DESTINATION_SETTLEMENT_TEXT = "Нас. пункт назначения";
    private final String DESTINATION_LOCATION_TEXT = "Место назначения";
    private final String DATE_TEXT = "Дата";
    private final String TIME_TEXT = "Время";
    private final String CAR_TEXT = "Автомобиль";
    private final String SEATS_QUANTITY_TEXT = "Количество мест";
    private final String SWAP_DEPARTURE_DESTINATION_TEXT = "Поменять выезд и назначение";
    private final String MY_RIDES_MENU_TEXT = "Мои поездки";
    //    TODO переименовать 2 следующих константы
    private final String AS_DRIVER_TEXT = "Поиск попутки";
    private final String AS_PASSENGER_TEXT = "Поиск пассажира";
    private final String EDIT_LAST_TEXT = "Редактировать последнее";
    private final String CANCEL_LAST_TEXT = "Отменить последнее";
    private final String CANCEL_REQUEST_TEXT = "Отменить запрос";


    //    Main menu buttons
    private final String MAIN_FIND_CAR = "Найти попутку";
    private final String MAIN_FIND_FELLOW = "Найти попутчика";
    private final String MAIN_GET_HELP = "Помощь";
    private final String MAIN_GET_USER_DATA = "Мои данные";

    //   Admins Main menu buttons
    private final String MAIN_ADMIN_ADD_SETTLEMENT = "Добавить нас. пункт";
    private final String MAIN_ADMIN_DELETE_SETTLEMENT = "Удалить нас. пункт";
    private final String MAIN_ADMIN_ADD_LOCATION = "Добавить локацию";
    private final String MAIN_ADMIN_DELETE_LOCATION = "Удалить локацию";
    private final String MAIN_ADMIN_SET_ADMIN = "Добавить администратора";
    private final String MAIN_ADMIN_BLOCK_USER = "Заблокировать пользователя";


    // Buttons attributes
    public Pair<String, String> buttonCreate(String buttonText, String buttonCallback) {
        return Pair.of(buttonText, buttonCallback);
    }

    public Pair<String, String> yesButtonCreate(String buttonCallback) {
        return Pair.of(YES_BUTTON_TEXT, buttonCallback);
    }

    public Pair<String, String> noButtonCreate(String buttonCallback) {
        return Pair.of(NO_BUTTON_TEXT, buttonCallback);
    }

    public Pair<String, String> editButtonCreate(String buttonCallback) {
        return Pair.of(EDIT_BUTTON_TEXT, buttonCallback);
    }

    public Pair<String, String> editLastButtonCreate(String buttonCallback) {
        return Pair.of(EDIT_LAST_TEXT, buttonCallback);
    }

    public Pair<String, String> cancelButtonCreate(String buttonCallback) {
        return Pair.of(CANCEL_BUTTON_TEXT, buttonCallback);
    }

    public Pair<String, String> cancelLastButtonCreate(String buttonCallback) {
        return Pair.of(CANCEL_LAST_TEXT, buttonCallback);
    }

    public Pair<String, String> cancelRequestButtonCreate(String buttonCallback) {
        return Pair.of(CANCEL_REQUEST_TEXT, buttonCallback);
    }

    public Pair<String, String> cancelButtonCreate() {
        return Pair.of(CANCEL_BUTTON_TEXT, Handlers.START.getHandlerPrefix() + Operation.CANCEL_CALLBACK);
    }

    public Pair<String, String> changeNameButtonCreate() {
        return Pair.of(CHANGE_NAME_TEXT, Handlers.USER.getHandlerPrefix() + UserOperation.EDIT_NAME_CALLBACK);
    }

    public Pair<String, String> changeResidenceButtonCreate() {
        return Pair.of(CHANGE_RESIDENCE_TEXT, Handlers.USER.getHandlerPrefix() + UserOperation.CHANGE_SETTLEMENT_REQUEST_CALLBACK);
    }

    public Pair<String, String> editCarButtonCreate() {
        return Pair.of(EDIT_CAR_TEXT, Handlers.CAR.getHandlerPrefix() + CarOperation.EDIT_CAR_REQUEST_CALLBACK);
    }

    public Pair<String, String> addCarButtonCreate() {
        return Pair.of(ADD_CAR_TEXT, Handlers.CAR.getHandlerPrefix() + CarOperation.ADD_CAR_REQUEST_CALLBACK);
    }

    public Pair<String, String> deleteCarButtonCreate() {
        return Pair.of(DELETE_CAR_TEXT, Handlers.CAR.getHandlerPrefix() + CarOperation.DELETE_CAR_REQUEST_CALLBACK);
    }

    public Pair<String, String> deleteButtonCreate() {
        return Pair.of(DELETE_ALL_TEXT, Handlers.USER.getHandlerPrefix() + UserOperation.DELETE_USER);
    }

    public Pair<String, String> deleteAllButtonCreate(String buttonCallback) {
        return Pair.of(DELETE_ALL_TEXT, buttonCallback);
    }

    public Pair<String, String> deleteButtonCreate(String buttonCallback) {
        return Pair.of(DELETE_TEXT, buttonCallback);
    }

    public Pair<String, String> skipButtonCreate(String buttonCallback) {
        return Pair.of(SKIP_STEP_TEXT, buttonCallback);
    }

    public Pair<String, String> saveButtonCreate(String buttonCallback) {
        return Pair.of(SAVE_BUTTON_TEXT, buttonCallback);
    }

    public Pair<String, String> modelButtonCreate(String buttonCallback) {
        return Pair.of(MODEL_TEXT, buttonCallback);
    }

    public Pair<String, String> colorButtonCreate(String buttonCallback) {
        return Pair.of(COLOR_TEXT, buttonCallback);
    }

    public Pair<String, String> platesButtonCreate(String buttonCallback) {
        return Pair.of(PLATES_TEXT, buttonCallback);
    }

    public Pair<String, String> commentaryButtonCreate(String buttonCallback) {
        return Pair.of(COMMENTARY_TEXT, buttonCallback);
    }

    public Pair<String, String> firstChoiceButtonCreate(String buttonCallback) {
        return Pair.of(FIRST_TEXT, buttonCallback);
    }

    public Pair<String, String> secondChoiceButtonCreate(String buttonCallback) {
        return Pair.of(SECOND_TEXT, buttonCallback);
    }

    public Pair<String, String> towardMinskButtonCreate(String buttonCallback) {
        return Pair.of(TOWARD_MINSK_TEXT, buttonCallback);
    }

    public Pair<String, String> fromMinskButtonCreate(String buttonCallback) {
        return Pair.of(FROM_MINSK_TEXT, buttonCallback);
    }

    public Pair<String, String> anotherButtonCreate(String buttonCallback) {
        return Pair.of(ANOTHER_TEXT, buttonCallback);
    }

    public Pair<String, String> todayButtonCreate(String buttonCallback) {
        return Pair.of(TODAY_TEXT, buttonCallback);
    }

    public Pair<String, String> tomorrowButtonCreate(String buttonCallback) {
        return Pair.of(TOMORROW_TEXT, buttonCallback);
    }

    public Pair<String, String> settlementLocationButtonCreate(String buttonCallback) {
        return Pair.of(SETTLEMENT_LOCATION_TEXT, buttonCallback);
    }

    public Pair<String, String> departureSettlementButtonCreate(String buttonCallback) {
        return Pair.of(DEPARTURE_SETTLEMENT_TEXT, buttonCallback);
    }

    public Pair<String, String> departureLocationButtonCreate(String buttonCallback) {
        return Pair.of(DEPARTURE_LOCATION_TEXT, buttonCallback);
    }

    public Pair<String, String> destinationSettlementButtonCreate(String buttonCallback) {
        return Pair.of(DESTINATION_SETTLEMENT_TEXT, buttonCallback);
    }

    public Pair<String, String> destinationLocationButtonCreate(String buttonCallback) {
        return Pair.of(DESTINATION_LOCATION_TEXT, buttonCallback);
    }

    public Pair<String, String> dateTimeButtonCreate(String buttonCallback) {
        return Pair.of(DATE_TIME_TEXT, buttonCallback);
    }

    public Pair<String, String> dateButtonCreate(String buttonCallback) {
        return Pair.of(DATE_TEXT, buttonCallback);
    }

    public Pair<String, String> timeButtonCreate(String buttonCallback) {
        return Pair.of(TIME_TEXT, buttonCallback);
    }

    public Pair<String, String> carDetailsButtonCreate(String buttonCallback) {
        return Pair.of(CAR_DETAILS_TEXT, buttonCallback);
    }

    public Pair<String, String> carButtonCreate(String buttonCallback) {
        return Pair.of(CAR_TEXT, buttonCallback);
    }

    public Pair<String, String> seatsQuantityButtonCreate(String buttonCallback) {
        return Pair.of(SEATS_QUANTITY_TEXT, buttonCallback);
    }

    public Pair<String, String> swapDepartureDestinationButtonCreate(String buttonCallback) {
        return Pair.of(SWAP_DEPARTURE_DESTINATION_TEXT, buttonCallback);
    }

    public Pair<String, String> myRidesButtonCreate() {
        return Pair.of(MY_RIDES_MENU_TEXT, Handlers.USER.getHandlerPrefix() + UserOperation.MY_RIDES_MENU);
    }

    public Pair<String, String> myRidesRequestButtonCreate() {
        return Pair.of(AS_PASSENGER_TEXT, Handlers.USER.getHandlerPrefix() + UserOperation.MY_RIDES_MENU);
    }

    public Pair<String, String> myPassengerRequestButtonCreate() {
        return Pair.of(AS_DRIVER_TEXT, Handlers.USER.getHandlerPrefix() + UserOperation.MY_RIDES_MENU);
    }

}
