package by.ivam.fellowtravelerbot.DTO;

import by.ivam.fellowtravelerbot.model.Car;
import by.ivam.fellowtravelerbot.model.DepartureLocation;
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
    private Settlement destinationSettlement;
    private DepartureLocation departureLocation;
    private Car car;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private String commentary;
}
