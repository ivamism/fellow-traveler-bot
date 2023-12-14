package by.ivam.fellowtravelerbot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Data
@Accessors(chain = true)
@Entity
public class Ride {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "find_passenger_request_id")
    @Column(nullable = false)
    private FindPassengerRequest findPassengerRequest;

    @OneToMany(orphanRemoval = true)
    @NotNull
    private Set<FindRideRequest> findRideRequests = new HashSet<>();
}
