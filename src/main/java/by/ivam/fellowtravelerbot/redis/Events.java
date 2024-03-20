package by.ivam.fellowtravelerbot.redis;

import lombok.Getter;

@Getter
public enum Events {

    NEW_HSET("hset"),
    EXPIRED("expired"),
    DELETED("del");

    private final String value;
    Events(String value) {
        this.value = value;
    }
}
