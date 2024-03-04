package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum RequestsType {
    FIND_RIDE_REQUEST("FRR"),
    FIND_PASSENGER_REQUEST("FPR"),
    NOT_REQUEST("NOT_REQUEST");
    private final String value;
    RequestsType(String value) {
        this.value = value;
    }
}
