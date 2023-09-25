package by.ivam.fellowtravelerbot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.*;

@Data
@Accessors(chain = true)
@Entity
public class FindRideRequest {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "user_chat_id")
    private User user;
    private int passengersQuantity;
    private String direction;
    @ManyToOne
    @JoinColumn(name = "departure_settlement_id")
    private Settlement departureSettlement;
    @ManyToOne
    @JoinColumn(name = "destination_settlement_id")
    private Settlement destinationSettlement;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private Duration departureDuring;
    private String commentary;
    private boolean isActive;
    private LocalDateTime createdAt;
    private boolean isCanceled;
    private LocalDateTime canceledAt;

}
