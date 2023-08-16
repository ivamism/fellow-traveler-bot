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
    // Buttons CallBackQueries

    private final String CANCEL_CALLBACK = "CANCEL_CALLBACK";


    //    User registration process
    private final String CONFIRM_START_REG_CALLBACK = "CONFIRM_START_REGISTRATION";
    private final String DENY_REG_CALLBACK = "DENY_REGISTRATION";
    private final String REG_USER_REQUEST_SETTLEMENT_CALLBACK = "REG_USER_REQUEST_SETTLEMENT_CALLBACK";
    private final String REG_USER_ADD_SETTLEMENT_CALLBACK = "REG_USER_ADD_SETTLEMENT_CALLBACK";
    private final String EDIT_REG_DATA_CALLBACK = "EDIT_REG_DATA";
    private final String NAME_TO_CONFIRM_CALLBACK = "CONFIRM_NAME";
    private final String NAME_CONFIRMED_CALLBACK = "NAME_CONFIRMED_CALLBACK";


    //     Handle User
    private final String EDIT_USER_CALLBACK = "EDIT_USER_CALLBACK";
    private final String EDIT_USER_NAME_CALLBACK = "EDIT_USER_NAME_CALLBACK";
    private final String EDIT_USER_RESIDENCE_CALLBACK = "EDIT_USER_RESIDENCE_CALLBACK";
    private final String DELETE_USER_START_PROCESS_CALLBACK = "DELETE_USER_START_PROCESS_CALLBACK";
    private final String DELETE_USER_CONFIRM_CALLBACK = "DELETE_USER_CONFIRM_CALLBACK";


    //    Add a Car process
    private final String ADD_CAR_CALLBACK = "ADD_CAR_CALLBACK";
    private final String ADD_CAR_START_CALLBACK = "ADD_CAR_START";
    private final String ADD_CAR_START_DENY_CALLBACK = "ADD_CAR_START_DENY";
    private final String ADD_CAR_SKIP_COMMENT_CALLBACK = "ADD_CAR_NO_COMMENT";
    private final String ADD_CAR_EDIT_CAR_CALLBACK = "ADD_CAR_EDIT_CAR";
    private final String ADD_CAR_SAVE_CAR_CALLBACK = "ADD_CAR_CONFIRM_CAR";

    // Deletion Car

    private final String DELETE_CAR_CALLBACK = "DELETE_CAR";
    private final String REQUEST_DELETE_CAR_CALLBACK = "REQUEST_DELETE_CAR";
    private final String DELETE_FIRST_CAR_CALLBACK = "DELETE_FIRST_CAR";
    private final String DELETE_SECOND_CAR_CALLBACK = "DELETE_SECOND_CAR";
    private final String DELETE_ALL_CARS_CALLBACK = "DELETE_ALL_CARS";

    //    Editing Car
    private final String ADD_CAR_EDIT_MODEL_CALLBACK = "ADD_CAR_EDIT_MODEL_CALLBACK";
    private final String ADD_CAR_EDIT_COLOR_CALLBACK = "ADD_CAR_EDIT_COLOR_CALLBACK";
    private final String ADD_CAR_EDIT_PLATES_CALLBACK = "ADD_CAR_EDIT_PLATES_CALLBACK";
    private final String ADD_CAR_EDIT_COMMENTARY_CALLBACK = "ADD_CAR_EDIT_COMMENTARY_CALLBACK";
    private final String EDIT_CAR_START_PROCESS_CALLBACK = "EDIT_CAR_START_PROCESS_CALLBACK";
    private final String EDIT_CAR_CHOOSE_FIRST_CAR_CALLBACK = "EDIT_CAR_CHOOSE_FIRST_CAR_CALLBACK";
    private final String EDIT_CAR_CHOOSE_SECOND_CAR_CALLBACK = "EDIT_CAR_CHOOSE_SECOND_CAR_CALLBACK";
    private final String EDIT_FIRST_CAR_EDIT_MODEL_CALLBACK = "EDIT_FIRST_CAR_EDIT_MODEL_CALLBACK";
    private final String EDIT_SECOND_CAR_EDIT_MODEL_CALLBACK = "EDIT_SECOND_CAR_EDIT_MODEL_CALLBACK";
    private final String EDIT_FIRST_CAR_EDIT_COLOR_CALLBACK = "EDIT_FIRST_CAR_EDIT_COLOR_CALLBACK";
    private final String EDIT_SECOND_CAR_EDIT_COLOR_CALLBACK = "EDIT_SECOND_CAR_EDIT_COLOR_CALLBACK";
    private final String EDIT_FIRST_CAR_EDIT_PLATES_CALLBACK = "EDIT_FIRST_CAR_EDIT_PLATES_CALLBACK";
    private final String EDIT_SECOND_CAR_EDIT_PLATES_CALLBACK = "EDIT_CAR_EDIT_PLATES_CALLBACK";
    private final String EDIT_FIRST_CAR_EDIT_COMMENTARY_CALLBACK = "EDIT_FIRST_CAR_EDIT_COMMENTARY_CALLBACK";
    private final String EDIT_SECOND_CAR_EDIT_COMMENTARY_CALLBACK = "EDIT_SECOND_CAR_EDIT_COMMENTARY_CALLBACK";

    //    AdminHandler
    private final String ADD_LOCATION_GET_SETTLEMENT_CALLBACK = "ADD_LOCATION_GET_SETTLEMENT_CALLBACK";


    // PickUpPassengerRequestHandler
    private final String CREATE_PICKUP_PASSENGER_REQUEST_CALLBACK = "CREATE_PICKUP_PASSENGER_REQUEST_CALLBACK";
    private final String CREATE_PICKUP_PASSENGER_REQUEST_DIRECTION_CALLBACK = "CREATE_PICKUP_PASSENGER_REQUEST_DIRECTION_CALLBACK";
    private final String CREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_CALLBACK = "CREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_CALLBACK";
    private final String CREATE_PICKUP_PASSENGER_REQUEST_ANOTHER_SETTLEMENT_CALLBACK = "CREATE_PICKUP_PASSENGER_REQUEST_ANOTHER_SETTLEMENT_CALLBACK";
    private final String CREATE_PICKUP_PASSENGER_REQUEST_DEPARTURE_LOCATION_CALLBACK = "CREATE_PICKUP_PASSENGER_REQUEST_DEPARTURE_LOCATION_CALLBACK";



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
    public Pair<String, String> yesButtonCreate(String buttonCallback) {
        return Pair.of(YES_BUTTON_TEXT, buttonCallback);
    }
    public Pair<String, String> noButtonCreate(String buttonCallback) {
        return Pair.of(NO_BUTTON_TEXT, buttonCallback);
    }
    public Pair<String, String> editButtonCreate(String buttonCallback) {
        return Pair.of(EDIT_BUTTON_TEXT, buttonCallback);
    }
    public Pair<String, String> cancelButtonCreate(String buttonCallback) {
        return Pair.of(CANCEL_BUTTON_TEXT, buttonCallback);
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
public Pair<String, String> deleteUserButtonCreate() {
        return Pair.of(DELETE_ALL_TEXT, Handlers.USER.getHandlerPrefix() + UserOperation.DELETE_USER);
    }
public Pair<String, String> deleteUserButtonCreate(String buttonCallback) {
        return Pair.of(DELETE_TEXT, buttonCallback);
    }
}
