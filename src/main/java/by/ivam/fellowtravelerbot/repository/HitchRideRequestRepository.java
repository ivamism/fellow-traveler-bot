package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.HitchRideRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HitchRideRequestRepository extends JpaRepository<HitchRideRequest, Integer> {
}