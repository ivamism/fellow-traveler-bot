package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.redis.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingService {

    Booking save(Booking booking);

    List<Booking> findAll();

    Booking findById(String bookingId);
    Optional<Booking>getBookingOptional(String bookingId);

    void incrementRemindsQuantityAndRemindTime(Booking booking);

    void deleteBooking(Booking booking);

    void deleteBooking(String bookingId);

    void deleteBookings(List<Booking> bookingsToDelete);

    boolean isNewRequest(Booking booking);

    void removeExpired();

    void removeBookingByCancelRequest(RequestsType initiator, int requestId);

    boolean hasBooking(RequestsType initiator, int requestId);
}
