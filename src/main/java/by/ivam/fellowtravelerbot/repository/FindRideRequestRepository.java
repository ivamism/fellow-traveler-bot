package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.FindRideRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FindRideRequestRepository extends JpaRepository<FindRideRequest, Integer> {
    Optional<FindRideRequest> findFirstByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(Long chatId);

    List<FindRideRequest> findByUser_ChatIdAndIsActiveTrueOrderByCreatedAtAsc(Long chatId);
//    List<FindPassengerRequest> findByUser_ChatIdAndIsActiveTrueOrderByDepartureAtAsc(Long chatId);

}