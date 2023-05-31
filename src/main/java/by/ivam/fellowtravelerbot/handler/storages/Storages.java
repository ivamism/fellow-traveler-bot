package by.ivam.fellowtravelerbot.handler.storages;

import by.ivam.fellowtravelerbot.DTO.RegUser;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Data
public class Storages {

HashMap<Long, String> activeChatsStorage = new HashMap<>();

HashMap<Long, String> userNamesStorage = new HashMap<>();

HashMap <Long, RegUser> regUserStorage = new HashMap<>();

}
