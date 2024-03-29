package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.FindRideRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface FindRideRequestRepository extends JpaRepository<FindRideRequest, Integer> {
    Optional<FindRideRequest> findFirstByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(Long chatId);

    List<FindRideRequest> findByUser_ChatIdAndIsActiveTrueOrderByCreatedAtAsc(Long chatId);

    List<FindRideRequest> findByDepartureBeforeBefore(LocalDateTime departureBefore);

    List<FindRideRequest> findByIsActiveTrueAndDepartureBeforeBefore(LocalDateTime departureBefore);
//    List<FindPassengerRequest> findByUser_ChatIdAndIsActiveTrueOrderByDepartureAtAsc(Long chatId);

}