package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum BotCommands {

    START("/start"),
    ADMIN_MENU("/showMasterAdminMenu"),
    HELP("/help"),
    HELP_BUTTON("Помощь"),
    PROFILE("/profile"),
    PROFILE_BUTTON("Мои данные"),
    REGISTRATION("/registration"),
    ADD_CAR("/add_car"),
    FIND_RIDE("Найти попутку"),
    FIND_PASSENGER("Найти попутчика"),
    ADD_SETTLEMENT("Добавить нас. пункт"),
    ADD_DEPARTURE_LOCATION("Добавить локацию");
    private final String command;

    BotCommands(String command) {
        this.command = command;
    }

}
