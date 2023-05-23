package by.ivam.fellowtravelerbot.handler.storage;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Data
public class BotStateStorage {

HashMap<Integer, String> activeChatsStorage = new HashMap<>();



}
