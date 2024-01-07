package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.BookingTemp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingCashRepository extends JpaRepository<BookingTemp, String> {
    List<BookingTemp> findByExpireAtBefore(LocalDateTime expireAt);

}