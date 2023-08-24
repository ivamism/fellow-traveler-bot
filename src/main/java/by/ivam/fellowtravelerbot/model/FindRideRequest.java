package by.ivam.fellowtravelerbot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalTime;

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
    private String direction;
    @ManyToOne
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private String commentary;
    boolean isActive;
}
