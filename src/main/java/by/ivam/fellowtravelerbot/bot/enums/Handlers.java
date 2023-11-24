package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum Handlers {
    START("START--"),
    ADMIN("ADMIN--"),
    USER("USER--"),
    CAR("CAR--"),
    FIND_RIDE("FIND_RIDE--"),
    FIND_PASSENGER("FIND_PAS--"),
    MATCHING("MATCHING--"),
    ;

    private final String handlerPrefix;
    Handlers(String handlerPrefix) {
        this.handlerPrefix = handlerPrefix;
    }
}
