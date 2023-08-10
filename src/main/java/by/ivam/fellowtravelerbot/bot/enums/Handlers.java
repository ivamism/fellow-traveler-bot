package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum Handlers {
    START("START-"),
    ADMIN("ADMIN-"),
    USER("USER-"),
    CAR("CAR-"),
    FIND_RIDE("FIND_RIDE-"),
    PICKUP_PASSENGER("PICKUP_PASSENGER-");

    private final String handlerPrefix;

    Handlers(String handlerPrefix) {
        this.handlerPrefix = handlerPrefix;
    }
}
