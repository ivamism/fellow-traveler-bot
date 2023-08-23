package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FindPassengerRequestRepository extends JpaRepository<FindPassengerRequest, Integer> {
}