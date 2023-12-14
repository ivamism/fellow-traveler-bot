package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum MatchingOperation {

    BOOK_REQUEST_CALLBACK("BOOK_REQUEST_CALLBACK:%s:%s:"),
    ACCEPT_BOOKING_CALLBACK("ACCEPT_BOOKING:"),
    DENY_BOOKING_CALLBACK("DENY_BOOKING:"),
    CHAT_WITH_PASSENGER_CALLBACK("CHAT_WITH_PASSENGER:"),
    ;

    private final String value;

    MatchingOperation(String value) {
        this.value = value;
    }
}
