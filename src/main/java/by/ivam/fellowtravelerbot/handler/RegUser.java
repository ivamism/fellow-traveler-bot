package by.ivam.fellowtravelerbot.handler;

import by.ivam.fellowtravelerbot.handler.enums.Handlers;
import by.ivam.fellowtravelerbot.handler.enums.RegUserProcessStatus;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

@Data
@Accessors(chain = true)
@Component
public class RegUser {
    private long chatId;
    private String firstName;
    private String telegramUserName;
    private Handlers handler = Handlers.USER_REGISTRATION;
    private RegUserProcessStatus regUserProcessStatus = RegUserProcessStatus.START_REGISTRATION;

}
