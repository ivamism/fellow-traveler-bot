package by.ivam.fellowtravelerbot.DTO;

import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.model.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Accessors(chain = true)

public class FindRideRequestDTO {

    private User user;
    private int passengersQuantity;
    private String direction;
    private Settlement departureSettlement;
    private Settlement desstinationSettlement;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private Duration departureDuring;
    private String commentary;
}
