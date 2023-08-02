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


    @ManyToOne
    @JoinColumn(name = "user_chat_id")
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(name = "direction")
    private Direction direction;
    @ManyToOne
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    LocalDate departureDate;
    LocalTime departureTime;

}
