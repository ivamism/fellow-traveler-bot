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
    private final String CONFIRM_START_REG_CALLBACK = "CONFIRM_START_REGISTRATION";
    private final String DENY_REG_CALLBACK = "DENY_REGISTRATION";
    private final String CONFIRM_REG_DATA_CALLBACK = "CONFIRM_REG_DATA";
    private final String EDIT_REG_DATA_CALLBACK = "EDIT_REG_DATA";
    private final String NAME_TO_CONFIRM_CALLBACK = "CONFIRM_NAME";
    private final String NAME_CONFIRMED_CALLBACK = "CONFIRM_NAME";


    // Buttons Name

    private final String YES_BUTTON_TEXT = "Да";
    private final String NO_BUTTON_TEXT = "Нет";
    private final String EDIT_BUTTON_TEXT = "Редактировать";
    private final String CANCEL_BUTTON_TEXT = "Отменить";
    private final String SEND_BUTTON_TEXT = "Отправить";


}
