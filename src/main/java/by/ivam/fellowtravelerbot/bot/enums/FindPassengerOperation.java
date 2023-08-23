package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum FindPassengerOperation {
    CREATE_FIND_PASSENGER_REQUEST_CALLBACK("CREATE_FIND_PASSENGER_REQUEST_CALLBACK"),
    CREATE_FIND_PASSENGER_REQUEST_DIRECTION_CALLBACK("CREATE_FIND_PAS_REQUEST_DIRECTION:");

    private final String value;
    FindPassengerOperation(String value) {
        this.value = value;
    }
}
