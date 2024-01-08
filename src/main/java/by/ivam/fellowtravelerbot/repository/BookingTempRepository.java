package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.BookingTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface BookingTempRepository extends JpaRepository<BookingTemp, String> {
    List<BookingTemp> findByExpireAtBefore(LocalDateTime expireAt);

}