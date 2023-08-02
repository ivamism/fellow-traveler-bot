package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum BotCommands {
    START("/start"),
    HELP("/help"),
    HELP_BUTTON("Помощь"),
    PROFILE("/profile"),
    PROFILE_BUTTON("Мои данные"),
    REGISTRATION("/registration"),
    ADD_CAR("/add_car"),
    FIND_RIDE("Найти попутку"),
    FIND_PASSENGER("Найти попутчика"),
    ADD_SETTLEMENT("Добавить нас. пункт"),
    ADD_DEPARTURE_LOCATION("Добавить локацию")
    ;
    BotCommands(String value) {
    }
}
