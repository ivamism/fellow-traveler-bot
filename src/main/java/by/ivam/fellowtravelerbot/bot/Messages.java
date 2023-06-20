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

//    User registration process
    private final String START_REGISTRATION = "Вы еще не зарегистрированы. Регистрируемся?";
    private final String CONFIRM_USER_FIRST_MESSAGE = "Поддвердите данные для регистрации: \n\n Ваше Имя - ";
    private final String DENY_REGISTRATION_MESSAGE = "Вы отказались от регистрации. До свидания";
    private final String SUCCESS_REGISTRATION_MESSAGE = "Вы успешно зарегистрированны. Для дальнейшего использования выберите действие из меню";
    private final String CONFIRM_FIRSTNAME_MESSAGE = "Вы ввели имя - ";
    private final String EDIT_USER_FIRSTNAME_MESSAGE = "Введите имя, под которым Вы хотите, чтобы Вас видели другие пользователи";

//    Add Car
    private final String ADD_CAR_START_MESSAGE = "Вы хотите добавить автомобиль в ваш профиль?";

    private final String ADD_CAR_DENY_START_MESSAGE = "Вы отказались от добавления автомобиля. Для продолжения работы с ботом выберите нужный пункт меню";
    private final String ADD_CAR_ADD_VENDOR_MESSAGE = "Впишите марку вашего автомобиля";
    private final String ADD_CAR_ADD_MODEL_MESSAGE = "Впишите модель вашего автомобиля";
    private final String ADD_CAR_ADD_COLOR_MESSAGE = "Впишите цвет вашего автомобиля";
    private final String ADD_CAR_ADD_PLATE_NUMBER_MESSAGE = "Впишите регистрационный номер вашего автомобиля";
    private final String ADD_CAR_ADD_COMMENTARY_MESSAGE = "Впишите коментарий\n (необязательное поле)";
    private final String ADD_CAR_CHECK_DATA_BEFORE_SAVE_MESSAGE = "Проверьте правильность введенных данных \n\n  Марка: %s\n  Модель: %s\n  Цвет: %s\n  Госномер: %s\n  Коментарий: %s\n\n" +
            "Для сохранения нажмите \"Сохранить\"\n" +
            "Для изменения нажмите \"Редактировать\"\n" +
            "Для отмены нажмите \"Отменить\"";
    private final String ADD_CAR_SAVE_SUCCESS_MESSAGE = "Ваш автомобиль успешно добавлен \n\n  Марка: %s\n  Модель: %s\n  Цвет: %s\n  Госномер: %s\n  Коментарий: %s\n" +
            "Для продолжения работы с ботом выберите нужный пункт меню.";

    //      DeleteCar
    private final String DELETE_CAR_START_MESSAGE = "У Вас добавлено максимально возможное количество автомобилей\n" +
            "Для того, чтобы добавить автомобиль необходимо удалить один из имеющихся\n" +
            "Хотите удалить?";




}
