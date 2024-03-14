package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.BookingTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingTempRepository extends JpaRepository<BookingTemp, String> {
    List<BookingTemp> findByExpireAtBefore(LocalDateTime expireAt);

    long deleteByExpireAtBefore(LocalDateTime now);

    Optional<BookingTemp> findByFindRideRequestId(int findRideRequestId);

    List<BookingTemp> findByFindPassengerRequestId(int findPassengerRequestId);

}