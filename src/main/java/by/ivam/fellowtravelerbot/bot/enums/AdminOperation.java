package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum AdminOperation {
    ADD_SETTLEMENT_NAME_CHAT_STATUS("ADD_SETTLEMENT_NAME_CHAT_STATUS"),
    DEPARTURE_LOCATION_SET_SETTLEMENT_CALLBACK("DEPARTURE_LOCATION_SET_SETTLEMENT_CALLBACK:"),
    DEPARTURE_LOCATION_REQUEST_NAME_CHAT_STATUS("DEPARTURE_LOCATION_REQUEST_NAME_CHAT_STATUS");

    private final String value;

    AdminOperation(String value) {
        this.value = value;
    }
}
