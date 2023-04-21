package by.ivam.fellowtravelerbot.handler.enums;

import lombok.Getter;
import org.springframework.stereotype.Component;

//@Component
@Getter
public enum RegUserProcessStatus {

    START_REGISTRATION,
    WAIT_CONFIRMATION,
    EDIT_NAME
}
