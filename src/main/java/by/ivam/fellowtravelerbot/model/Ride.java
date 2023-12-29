package by.ivam.fellowtravelerbot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private FindPassengerRequest findPassengerRequest;

    @OneToMany(orphanRemoval = true)
    private Set<FindRideRequest> findRideRequests = new LinkedHashSet<>();

}
