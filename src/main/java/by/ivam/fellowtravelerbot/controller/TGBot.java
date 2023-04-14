package by.ivam.fellowtravelerbot.controller;

import by.ivam.fellowtravelerbot.config.BotConfig;
import by.ivam.fellowtravelerbot.servise.CarService;
import by.ivam.fellowtravelerbot.servise.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
@Log4j
public class TGBot extends TelegramLongPollingBot {
    @Autowired
    final BotConfig botConfig;

    @Autowired
    CarService carService;

    @Autowired
    UserService userService;

    @Autowired
    Keyboards keyboards;

    static final String HELP_TEXT = """
            Это бот для поиска попутных пассажиров или машин из деревень Королево, Озеро и Дещенка в Минск и обратно.

            Меню для работы с ботом на синей кнопке внизу экрана

            Выберите /start для запуска бота

            Выберите /registration для регистрации нового пользователя

            Выберите /car_registration для добавления автомобиля

            Выберите /profile для просмотра сохраненных данных о себе"

            Выберите /new_trip что бы начать поиск попутчиков             
                         
            Выберите /feedback для связи с разработчиком

            """;
    static final String ERROR_TEXT = "ERROR: ";


    public TGBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "Запускаем бот"));
        botCommandList.add(new BotCommand("/help", "Информация об использовании бота"));
        botCommandList.add(new BotCommand("/registration", "Регистрация нового пользователя"));
        botCommandList.add(new BotCommand("/car_registration", "добавление автомобиля"));
        botCommandList.add(new BotCommand("/profile", "Посмотреть сохраненые данные"));
        botCommandList.add(new BotCommand("/new_trip", "Запланировать новую поездку"));
        botCommandList.add(new BotCommand("//feedback", "связаться с разработчиком"));
        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start" -> {
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    log.info("Start Bot with " + update.getMessage().getChat().getUserName()
                            + ". ChatId: " + messageText);
                }
                case "/help" -> {
                    sendMessage(chatId, HELP_TEXT);
                    log.debug("get Message: " + messageText);
                }

                case "/registration" -> {
                    log.debug("get Message: " + messageText + " - Start registration process");
                    registerUser(chatId, );
                }
                default -> {
                    sendMessage(chatId, "Sorry this option still doesn't work");
                    log.debug("get Message: " + update.getMessage().getText());
                }
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
       String answer = "Hi," + firstName + "!";

        sendMessage(chatId, answer);

    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void registerUser(long chatId) {




//        {
//            SendMessage message = new SendMessage();
////            String userFirstName =
//            message.setChatId((chatId));
//            message.setText("Поддвердите данные для регистрации: \n\n Ваше Имя - " +);
//        }




//        message.setReplyMarkup(markupInLine);

//        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }
}
