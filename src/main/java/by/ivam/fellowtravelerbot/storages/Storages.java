package by.ivam.fellowtravelerbot.storages;

import by.ivam.fellowtravelerbot.DTO.CarDTO;
import by.ivam.fellowtravelerbot.DTO.DepartureLocationDTO;
import by.ivam.fellowtravelerbot.DTO.HitchRideRequestDTO;
import by.ivam.fellowtravelerbot.DTO.UserDTO;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Data
public class Storages {

HashMap<Long, String> activeChatsStorage = new HashMap<>();

HashMap<Long, String> userNamesStorage = new HashMap<>();

HashMap <Long, UserDTO> userDTOStorage = new HashMap<>();
HashMap <Long, CarDTO> addCarStorage = new HashMap<>();
HashMap <Long, DepartureLocationDTO> addDepartureLocationStorage = new HashMap<>();
HashMap <Long, HitchRideRequestDTO> addHitchRideRequestStorage = new HashMap<>();

}
