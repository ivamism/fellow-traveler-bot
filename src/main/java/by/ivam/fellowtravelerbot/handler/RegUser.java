package by.ivam.fellowtravelerbot.handler;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

@Data
@Accessors(chain = true)
@Component
public class RegUser {
    private long chatId;
    private String firstName;
    private String telegramUserName;
//    private Handlers handler = Handlers.USER_REGISTRATION;
//    private BotStatus regUserProcessStatus = BotStatus.START_REGISTRATION;

}
