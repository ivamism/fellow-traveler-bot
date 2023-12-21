package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.bot.enums.BookingInitiator;
import by.ivam.fellowtravelerbot.redis.model.Booking;

import java.util.List;

public interface BookingService {

    Booking save(Booking booking);

    List<Booking> findAll();

    Booking findById(String bookingId);

    void incrementRemindsQuantityAndRemindTime(Booking booking);

    void deleteBooking(Booking booking);

    void deleteBooking(String bookingId);

    boolean isNewRequest(Booking booking);

    void removeExpired();

    void cancelBooking(BookingInitiator initiator, int requestId);

    boolean hasBooking(BookingInitiator initiator, int requestId);
}
