package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum Direction {
    TOWARDS_MINSK("TOWARDS_MINSK"),
    FROM_MINSK("FROM_MINSK");
    private final String value;

    Direction(String value) {
        this.value = value;
    }
}
