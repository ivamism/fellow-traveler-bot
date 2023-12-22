package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.BookingCash;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingCashRepository extends JpaRepository<BookingCash, String> {
    List<BookingCash> findByExpireAtBefore(LocalDateTime expireAt);

}