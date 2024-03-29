package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RideRepository extends JpaRepository<Ride, Integer> {
    Optional<Ride> findByFindPassengerRequest_Id(int id);

    boolean existsByFindPassengerRequest_Id(int id);

    boolean existsByFindRideRequests_Id(int id);


}