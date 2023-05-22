package by.ivam.fellowtravelerbot.handler.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Getter
@RequiredArgsConstructor
public enum BotStatus {

    REGISTRATION_START,
    WAIT_CONFIRMATION,
    REGISTRATION_EDIT_NAME;


}
