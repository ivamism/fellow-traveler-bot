package by.ivam.fellowtravelerbot.DTO;

import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.model.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)

public class FindRideRequestDTO {

    private User user;
    private String direction;
    private Settlement departureSettlement;
    private Settlement destinationSettlement;
    private LocalDateTime departureBefore;
//    private Duration departureDuring;
    private int passengersQuantity;
    private String commentary;
}
