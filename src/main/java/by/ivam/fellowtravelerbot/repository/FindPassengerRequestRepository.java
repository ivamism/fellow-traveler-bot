package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FindPassengerRequestRepository extends JpaRepository<FindPassengerRequest, Integer> {
    List<FindPassengerRequest> findByUser_ChatIdAndIsActiveTrue(Long chatId);

    List<FindPassengerRequest> findByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(Long chatId);

    List<FindPassengerRequest> findByUser_ChatIdAndIsActiveTrueOrderByDepartureAtAsc(Long chatId);

    FindPassengerRequest findFirstByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(Long chatId);




}