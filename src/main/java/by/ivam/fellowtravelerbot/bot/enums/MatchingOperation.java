package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum MatchingOperation {
    ACCEPT_FIND_RIDE_REQUEST_CALLBACK("ACCEPT_FIND_RIDE_REQUEST:%s:"),
    ACCEPT_FIND_PASS_REQUEST_CALLBACK("ACCEPT_FIND_PASS_REQUEST:%s:"),
    CHAT_WITH_PASSENGER_CALLBACK("CHAT_WITH_PASSENGER:"),
    ;

    private final String value;

    MatchingOperation(String value) {
        this.value = value;
    }
}
