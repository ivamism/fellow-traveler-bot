package by.ivam.fellowtravelerbot.bot;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class Messages {

    private final String HELP_TEXT = """
            Это бот для поиска попутных пассажиров или машин из деревень Королево, Озеро и Дещенка в Минск и обратно.

            Меню для работы с ботом на синей кнопке внизу экрана

            Выберите /start для запуска бота

            Выберите /registration для регистрации нового пользователя

            Выберите /car_registration для добавления автомобиля

            Выберите /profile для просмотра сохраненных данных о себе

            Выберите /new_trip что бы начать поиск попутчиков
                         
            Выберите /feedback для связи с разработчиком

            """;
    private final String ERROR_TEXT = "ERROR: ";
    private final String CHOOSE_ACTION = "Выберите действие из меню";
    private final String START_REGISTRATION = "Вы еще не зарегистрированы. Регистрируемся?";
    private final String CONFIRM_REG_DATA_MESSAGE = "Поддвердите данные для регистрации: \n\n Ваше Имя - ";
    private final String DENY_REG_DATA_MESSAGE = "Вы отказались от регистрации. До свидания";

    // Buttons
    private final String CONFIRM_REG_CALLBACK = "CONFIRM_REGISTRATION";
    private final String DENY_REG_CALLBACK = "DENY_REGISTRATION";
    private final String CONFIRM_REG_DATA_CALLBACK = "CONFIRM_REG_DATA";
    private final String EDIT_REG_DATA_CALLBACK = "EDIT_REG_DATA";

    // Buttons Name

    private final String YES_BUTTON_TEXT = "Да";
    private final String NO_BUTTON_TEXT = "Нет";
    private final String EDIT_BUTTON_TEXT = "Редактировать";

}
