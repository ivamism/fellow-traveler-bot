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
    private final String FURTHER_ACTION_MESSAGE = "Для продолжения работы с ботом выберите нужный пункт меню.";
    private final String ADMIN_MESSAGE = "Вы вошли в меню администратора.\nВыберите необходимое действие на главной клавиатуре.";
    private final String NO_REGISTRATION_MESSAGE = "Вы еще не зарегистрированы." +
            "Для регистрации выберите /start, или /registration";
    private final String ACTUAL_VALUE_MESSAGE = "\n  Текущее значение: %s";

    //    User registration process
    private final String START_REGISTRATION = "Вы еще не зарегистрированы. Регистрируемся?";
    private final String CONFIRM_USER_FIRST_MESSAGE = "Поддвердите данные для регистрации: \n\n Ваше Имя - ";
    private final String DENY_REGISTRATION_MESSAGE = "Вы отказались от регистрации. До свидания";
    private final String SUCCESS_REGISTRATION_MESSAGE = "Вы успешно зарегистрированны. Для дальнейшего использования выберите действие из меню";
    private final String CONFIRM_FIRSTNAME_MESSAGE = "Вы ввели имя - ";
    private final String EDIT_USER_FIRSTNAME_MESSAGE = "Введите имя, под которым Вы хотите, чтобы Вас видели другие пользователи.\n";
    private final String EDIT_USER_FIRSTNAME_SUCCESS_MESSAGE = "Ваше текущее имя:\n%s\n";
    private final String EDIT_USER_RESIDENCE_SUCCESS_MESSAGE = "Ваше текущее место жительства:\n%s\n";

    //    Handle User
    private final String USER_DATA = "Данные пользователя:\n\n  Id: %d\n  Имя: %s\n  ТГ имя: %s\n  Место жительства: %s \n\nАвтомобили:\n";
    private final String EDIT_USER_FIRSTNAME_MESSAGE_POSTFIX = "Ваше текущее имя: %s";
    private final String DELETE_USER_START_MESSAGE = """
            Нажмите "Удалить", если вы хотите удалить ваши данные из бота.
            Если вы захотите вернуться к использованию бота в будущем, выберите команду /start.
            Нажмите "Отменить", если вы хотите продолжить пользоваться ботом.""";
    private final String DELETE_USER_DONE_MESSAGE = """
            Ваши данные удалены.
            Если вы захотите вернуться к использованию бота в будущем, выберите команду /start.
            """;

    //    Add Car
    private final String ADD_CAR_START_MESSAGE = "Вы хотите добавить автомобиль в ваш профиль?";

    private final String ADD_CAR_DENY_START_MESSAGE = "Вы отказались от добавления автомобиля. Для продолжения работы с ботом выберите нужный пункт меню";
    private final String ADD_CAR_ADD_VENDOR_MESSAGE = "Впишите марку вашего автомобиля";
    private final String ADD_CAR_ADD_MODEL_MESSAGE = "Впишите марку и модель вашего автомобиля";
    private final String ADD_CAR_ADD_COLOR_MESSAGE = "Впишите цвет вашего автомобиля";
    private final String ADD_CAR_ADD_PLATE_NUMBER_MESSAGE = "Впишите регистрационный номер вашего автомобиля";
    private final String ADD_CAR_ADD_COMMENTARY_MESSAGE = "Впишите коментарий\n (необязательное поле)";
    private final String ADD_CAR_CHECK_DATA_BEFORE_SAVE_MESSAGE = """
            Проверьте правильность введенных данных\s

              Модель: %s
              Цвет: %s
              Госномер: %s
              Коментарий: %s

            Для сохранения нажмите "Сохранить"
            Для изменения нажмите "Редактировать"
            Для отмены нажмите "Отменить\"""";
    private final String ADD_CAR_SAVE_SUCCESS_PREFIX_MESSAGE = "Ваш автомобиль успешно добавлен \n\n";

    private final String SHOW_CAR_MESSAGE = "  Модель: %s\n    Цвет: %s\n    Госномер: %s\n    Коментарий: %s\n";

    //      Delete Car
    private final String DELETE_CAR_START_MESSAGE = """
            У Вас добавлено максимально возможное количество автомобилей.
            Для того, чтобы добавить автомобиль необходимо удалить один из имеющихся.
            Хотите удалить?""";
    private final String DELETE_CAR_CHOOSE_MESSAGE = "Выберите автомобиль для удаления.\n\n";
    private final String DELETE_CAR_POSTFIX_MESSAGE = "У Вас добавлено максимально возможное количество автомобилей.\n";
    private final String DELETE_ALL_CARS_MESSAGE = "Все Ваши автомобили удалены.\n";


    //    Edit Car
    private final String EDIT_CAR_START_MESSAGE = "Выберите, что вы хотите изменить:\n";

    private final String EDIT_CAR_CHOICE_PREFIX_MESSAGE = "Выберите, какой автомобиль вы хотите изменить:\n";
    private final String EDIT_CAR_CHOSEN_PREFIX_MESSAGE = "Вы выбрали:\n%s\n";
    private final String EDIT_CAR_SUCCESS_PREFIX_MESSAGE = "Изменения успешно внесены\n";


    // AdminHandler
    private final String ADD_SETTLEMENT_NAME_MESSAGE = "Впишите название населенного пункта";
    private final String ADD_SETTLEMENT_SUCCESS_MESSAGE = "Населенный пункт:\n\n  Id: %d\n  Название: %s\n  успешно сохранен";
    private final String ADD_LOCATION_CHOOSE_SETTLEMENT_MESSAGE = "Выберите населенный пункт";
    private final String ADD_LOCATION_NAME_MESSAGE = "Впишите название места выезда";
    private final String ADD_LOCATION_SUCCESS_MESSAGE = "Место выезда:\n\n  Id: %d\n  Название: %s\n  Населенный пункт: %s\n  успешно сохранено";

    //    FindPassengerRequest
//    Creation new request
    private final String CREATE_PICKUP_PASSENGER_REQUEST_START_PROCESS_MESSAGE = "Хотите найти попутчика?";
    private final String CREATE_PICKUP_PASSENGER_REQUEST_DIRECTION_MESSAGE = "Выберите направление поездки";
    private final String CREATE_PICKUP_PASSENGER_REQUEST_SETTLEMENT_MESSAGE = "Выберите населенный пункт";
    private final String CREATE_PICKUP_PASSENGER_REQUEST_DEPARTURE_LOCATION_MESSAGE = "Выберите место выезда";

}
