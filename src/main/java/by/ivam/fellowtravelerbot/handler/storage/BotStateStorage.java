package by.ivam.fellowtravelerbot.handler.storage;

import by.ivam.fellowtravelerbot.handler.storage.BotState;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Data
public class BotStateStorage {

Map<Integer, String> activeChatsStorage = new HashMap<>();

}
