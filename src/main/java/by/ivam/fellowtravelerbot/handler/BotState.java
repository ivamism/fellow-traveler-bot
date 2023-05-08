package by.ivam.fellowtravelerbot.handler;

import by.ivam.fellowtravelerbot.handler.enums.BotStatus;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class BotState {
//   private long chatId;
   private int messageId;
   private BotStatus botStatus;
}
