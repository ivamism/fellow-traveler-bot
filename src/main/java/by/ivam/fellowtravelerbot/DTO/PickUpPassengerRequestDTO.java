package by.ivam.fellowtravelerbot.DTO;

import by.ivam.fellowtravelerbot.model.Car;
import by.ivam.fellowtravelerbot.model.DepartureLocation;
import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Accessors(chain = true)
public class PickUpPassengerRequestDTO {

    @ManyToOne
    @JoinColumn(name = "user_chat_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;
    @ManyToOne
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @ManyToOne
    @JoinColumn(name = "departure_location_id")
    private DepartureLocation departureLocation;

    LocalDate departureDate;
    LocalTime departureTime;
    boolean isActive;

}
