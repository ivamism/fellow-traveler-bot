package by.ivam.fellowtravelerbot.DTO;

import by.ivam.fellowtravelerbot.servise.handler.enums.ChatStatus;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

@Data
@Accessors(chain = true)
@Component
@ToString
public class UserDTO {
    private long chatId;
    private String firstName;
    private String telegramUserName;

    private ChatStatus botStatus;

}
