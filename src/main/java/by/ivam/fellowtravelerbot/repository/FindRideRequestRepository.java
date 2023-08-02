package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.FindRideRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FindRideRequestRepository extends JpaRepository<FindRideRequest, Integer> {
}