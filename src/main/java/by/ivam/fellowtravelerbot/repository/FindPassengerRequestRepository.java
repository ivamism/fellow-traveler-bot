package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FindPassengerRequestRepository extends JpaRepository<FindPassengerRequest, Integer> {
    List<FindPassengerRequest> findByUser_ChatIdAndIsActiveTrue(Long chatId);

}