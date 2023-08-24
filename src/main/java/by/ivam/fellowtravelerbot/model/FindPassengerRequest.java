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
    @JoinColumn()
    private Settlement departureSettlement;

    @ManyToOne
    @JoinColumn()
    private Settlement destinationSettlement;
    @ManyToOne
    @JoinColumn(name = "departure_location_id")
    private DepartureLocation departureLocation;
    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    private LocalDate departureDate;
    private LocalTime departureTime;

    private String commentary;
    private boolean isActive;
}
