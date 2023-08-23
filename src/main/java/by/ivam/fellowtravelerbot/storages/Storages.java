package by.ivam.fellowtravelerbot.storages;

import by.ivam.fellowtravelerbot.DTO.*;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Data
public class Storages {

HashMap<Long, String> chatsStatusStorage = new HashMap<>();

HashMap<Long, String> userNamesStorage = new HashMap<>();

 HashMap <Long, UserDTO> userDTOStorage = new HashMap<>();
HashMap <Long, CarDTO> CarDTOStorage = new HashMap<>();
HashMap <Long, DepartureLocationDTO> departureLocationDTOStorage = new HashMap<>();
HashMap <Long, FindRideRequestDTO> hitchRideRequestDTOStorage = new HashMap<>();
HashMap <Long, FindPassengerRequestDTO> pickUpPassengerRequestDTOStorage = new HashMap<>();

}
