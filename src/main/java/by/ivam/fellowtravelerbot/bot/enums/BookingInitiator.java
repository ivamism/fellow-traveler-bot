package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum BookingInitiator {
    FIND_RIDE_REQUEST("FRR"),
    FIND_PASSENGER_REQUEST("FPR");
    private final String value;
    BookingInitiator(String value) {
        this.value = value;
    }
}
