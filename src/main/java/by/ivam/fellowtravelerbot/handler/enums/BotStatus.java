package by.ivam.fellowtravelerbot.handler.enums;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public enum BotStatus {

    NO_STATUS,
    REGISTRATION_START,
    WAIT_CONFIRMATION,
    REGISTRATION_EDIT_NAME
}
