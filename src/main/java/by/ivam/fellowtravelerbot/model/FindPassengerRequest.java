package by.ivam.fellowtravelerbot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Accessors(chain = true)
@Entity
public class FindPassengerRequest {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_chat_id")
    private User user;
    private String direction;
    @ManyToOne
    @JoinColumn(name = "departure_settlement_id")
    private Settlement departureSettlement;

    @ManyToOne
    @JoinColumn(name = "destination_settlement_id")
    private Settlement destinationSettlement;

    @ManyToOne
    @JoinColumn(name = "departure_location_id")
    private Location departureLocation;

    @ManyToOne
    @JoinColumn(name = "destination_location_id")
    private Location destinationLocation;
    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;
    private int seatsQuantity;

    private LocalDate departureDate;
    private LocalTime departureTime;

    @Column(length = 1000)
    private String commentary;
    private boolean isActive;
}
