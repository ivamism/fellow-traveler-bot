package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.model.BookingTemp;
import by.ivam.fellowtravelerbot.redis.model.Booking;

import java.util.Optional;

public interface BookingTempService {
    void saveBookingTemp(Booking booking);
    Optional<BookingTemp> findById (String id);
    void setCanceledBy(RequestsType type, int requestId);
    void flushExpired ();
}
