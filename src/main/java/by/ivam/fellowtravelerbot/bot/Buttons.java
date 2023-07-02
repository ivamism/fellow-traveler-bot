package by.ivam.fellowtravelerbot.bot;
/*
This class contains Strings of buttons names and callback-queries which used in keyboards
 */

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class Buttons {
    // Buttons CallBackQueries

    //    User registration process
    private final String CONFIRM_START_REG_CALLBACK = "CONFIRM_START_REGISTRATION";
    private final String DENY_REG_CALLBACK = "DENY_REGISTRATION";
    private final String CONFIRM_REG_DATA_CALLBACK = "CONFIRM_REG_DATA";
    private final String EDIT_REG_DATA_CALLBACK = "EDIT_REG_DATA";
    private final String NAME_TO_CONFIRM_CALLBACK = "CONFIRM_NAME";
    private final String NAME_CONFIRMED_CALLBACK = "CONFIRM_NAME";

    //    Add a Car process
    private final String ADD_CAR_START_CALLBACK = "ADD_CAR_START";
    private final String ADD_CAR_START_DENY_CALLBACK = "ADD_CAR_START_DENY";
    private final String ADD_CAR_SKIP_COMMENT_CALLBACK = "ADD_CAR_NO_COMMENT";
    private final String ADD_CAR_EDIT_CAR_CALLBACK = "ADD_CAR_EDIT_CAR";
    private final String ADD_CAR_SAVE_CAR_CALLBACK = "ADD_CAR_CONFIRM_CAR";

    // Deletion Car

    private final String DELETE_CAR_CALLBACK = "DELETE_CAR";
    private final String REQUEST_DELETE_CAR_CALLBACK = "REQUEST_DELETE_CAR";
    private final String DENY_DELETE_CAR_CALLBACK = "DENY_DELETE_CAR";
    private final String DELETE_FIRST_CAR_CALLBACK = "DELETE_FIRST_CAR";
    private final String DELETE_SECOND_CAR_CALLBACK = "DELETE_SECOND_CAR";
    private final String DELETE_ALL_CARS_CALLBACK = "DELETE_ALL_CARS";

    //    Editing Car
    private final String EDIT_CAR_CALLBACK = "EDIT_CAR_CAR";
    private final String ADD_CAR_EDIT_MODEL_CALLBACK = "ADD_CAR_EDIT_MODEL_CALLBACK";
    private final String ADD_CAR_EDIT_COLOR_CALLBACK = "ADD_CAR_EDIT_COLOR_CALLBACK";
    private final String ADD_CAR_EDIT_PLATES_CALLBACK = "ADD_CAR_EDIT_PLATES_CALLBACK";
    private final String ADD_CAR_EDIT_COMMENTARY_CALLBACK = "ADD_CAR_EDIT_COMMENTARY_CALLBACK";


    // Buttons Names

    private final String YES_BUTTON_TEXT = "Да";
    private final String NO_BUTTON_TEXT = "Нет";
    private final String SAVE_BUTTON_TEXT = "Сохранить";
    private final String EDIT_BUTTON_TEXT = "Редактировать";
    private final String CANCEL_BUTTON_TEXT = "Отменить";
    private final String SEND_BUTTON_TEXT = "Отправить";
    private final String SKIP_STEP_TEXT = "Пропустить";
    private final String DELETE_TEXT = "Удалить";
    private final String DELETE_ALL_TEXT = "Удалить всё";
    private final String FIRST_TEXT = String.valueOf(1);
    private final String SECOND_TEXT = String.valueOf(2);
    private final String MODEL_TEXT = "Модель";
    private final String COLOR_TEXT = "Цвет";
    private final String PLATES_TEXT = "Рег. номер";
    private final String COMMENTARY_TEXT = "Комментарий";

//    Main menu buttons
    private final String MAIN_FIND_CAR = "Найти машину";
    private final String MAIN_FIND_FELLOW = "Найти пассажира";
    private final String MAIN_GET_HELP = "Помощь";
    private final String MAIN_GET_USER_DATA = "Мои данные";


}
