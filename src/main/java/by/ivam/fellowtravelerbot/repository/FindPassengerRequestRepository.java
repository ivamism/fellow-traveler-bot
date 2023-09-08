package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FindPassengerRequestRepository extends JpaRepository<FindPassengerRequest, Integer> {
    List<FindPassengerRequest> findByUser_ChatIdAndIsActiveTrueOrderByDepartureAtAsc(Long chatId);

    Optional <FindPassengerRequest> findFirstByUser_ChatIdAndIsActiveTrueOrderByCreatedAtDesc(Long chatId);





}