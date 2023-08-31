package by.ivam.fellowtravelerbot.DTO;

import by.ivam.fellowtravelerbot.model.Car;
import by.ivam.fellowtravelerbot.model.Location;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.model.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Accessors(chain = true)
public class FindPassengerRequestDTO {

    private User user;
    private String direction;
    private Settlement departureSettlement;
    private Location departureLocation;
    private Settlement destinationSettlement;
    private Location destinationLocation;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private Car car;
    private int seatsQuantity;
    private String commentary;
}
