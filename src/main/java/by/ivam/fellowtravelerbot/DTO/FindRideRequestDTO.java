package by.ivam.fellowtravelerbot.DTO;

import by.ivam.fellowtravelerbot.model.Direction;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Accessors(chain = true)

public class FindRideRequestDTO {

    private User user;
    private Direction direction;
    private Settlement settlement;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private String commentary;
}
