package by.ivam.fellowtravelerbot.DTO;

import by.ivam.fellowtravelerbot.model.Settlement;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

@Data
@Accessors(chain = true)
@Component
public class UserDTO {
    private long chatId;
    private String firstName;
    private String telegramUserName;
    private Settlement residence;
}
