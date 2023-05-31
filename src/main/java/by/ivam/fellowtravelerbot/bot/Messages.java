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
    private final String UNKNOWN_COMMAND = "Неизвестная команда";
    private final String CHOOSE_ACTION = "Выберите действие из меню";
    private final String START_REGISTRATION = "Вы еще не зарегистрированы. Регистрируемся?";
    private final String CONFIRM_USER_FIRST_MESSAGE = "Поддвердите данные для регистрации: \n\n Ваше Имя - ";
    private final String DENY_REGISTRATION_MESSAGE = "Вы отказались от регистрации. До свидания";
    private final String SUCCESS_REGISTRATION_MESSAGE = "Вы успешно зарегистрированны. Для дальнейшего использования выберите действие из меню";
    private final String CONFIRM_FIRSTNAME_MESSAGE = "Вы ввели имя - ";
    private final String EDIT_USER_FIRSTNAME_MESSAGE = "Введите имя, под которым Вы хотите, чтобы Вас видели другие пользователи";

}
