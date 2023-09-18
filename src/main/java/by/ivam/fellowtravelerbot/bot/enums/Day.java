package by.ivam.fellowtravelerbot.bot.enums;

import lombok.Getter;

@Getter
public enum Day {
    TODAY("TODAY"),
    TOMORROW("TOMORROW");
    private final String value;
    Day(String value) {
        this.value = value;
    }
}
