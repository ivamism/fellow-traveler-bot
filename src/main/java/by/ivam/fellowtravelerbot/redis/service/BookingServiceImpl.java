package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.model.BookingTemp;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.repository.BookingRepository;
import by.ivam.fellowtravelerbot.servise.BookingTempService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static by.ivam.fellowtravelerbot.bot.enums.RequestsType.FIND_PASSENGER_REQUEST;
import static by.ivam.fellowtravelerbot.bot.enums.RequestsType.FIND_RIDE_REQUEST;

@Service
@Log4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository repository;

    @Autowired
    private FindPassRequestRedisService findPassRequestRedisService;
    @Autowired
    private FindRideRequestRedisService findRideRequestRedisService;
    @Autowired
    private BookingTempService bookingTempService;

    @Override
    public Booking save(Booking booking) {
        repository.save(booking);
        log.debug("save booking: " + booking);
        return booking;
    }

    @Override
    public List<Booking> findAll() {
        List<Booking> bookingList = StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .filter(booking -> Optional.ofNullable(booking).isPresent())
                .collect(Collectors.toList());
        log.debug("findAll. bookings found: " + bookingList.size());
        return bookingList;
    }

    @Override
    public Booking findById(String bookingId) {
        Booking booking = repository.findById(bookingId).orElseThrow();
        log.debug("method findById: " + booking);
        return booking;
    }

    @Override
    public Optional<Booking> getBookingOptional(String bookingId) {
        log.debug("method getBookingOptional");
        return repository.findById(bookingId);
    }

    @Override
    public void incrementRemindsQuantityAndRemindTime(Booking booking) {
        findById(booking.getId());
        booking.setRemindersQuantity(booking.getRemindersQuantity() + 1);
        booking.setRemindAt(LocalDateTime.now().plusMinutes(1));
        repository.save(booking);
        log.debug("incrementRemindsQuantityAndRemindTime: " + booking);
    }

    @Override
    public void deleteBooking(Booking booking) {
        log.debug("delete booking: " + booking);
        increaseSeatsQuantity(booking);
        repository.delete(booking);
    }

    @Override
    public void deleteBooking(String bookingId) {
        log.debug("delete booking: " + bookingId);
        increaseSeatsQuantity(repository.findById(bookingId).orElseThrow());
        repository.deleteById(bookingId);
    }

    @Override
    public void deleteBookings(List<Booking> bookingsToDelete) {
        // Suggestion 2: Log the number of bookings to be deleted
        log.debug("Deleting " + bookingsToDelete.size() + " bookings");
    
        // Suggestion 3: Call increaseSeatsQuantity for each booking
      /*  for (Booking booking : bookingsToDelete) {
            increaseSeatsQuantity(booking);
        }*/
    
        // Suggestion 1: Use repository.deleteAll to delete all bookings in one operation
        repository.deleteAll(bookingsToDelete);
    
        // Suggestion 4: Handle exceptions that may occur during the deletion process
//        try {
//            for (Booking booking : bookingsToDelete) {
//                repository.delete(booking);
////                increaseSeatsQuantity(booking);
//            }
//        } catch (Exception e) {
//            log.error("Error occurred during deletion: " + e.getMessage());
//            // Handle the exception or rethrow it
//        }
    }

    @Override
    public boolean isNewRequest(Booking booking) {
        return booking.getRemindersQuantity() == 0;
    }

    @Override
    public void removeExpired() {
        List<Booking> expiredKeys = repository.findByExpireDuration(-1);
        if (expiredKeys.size() != 0) {
            log.info("remove expired FindRideRequestRedis - " + expiredKeys.size());
            expiredKeys.forEach(booking -> deleteBooking(booking));
        }
    }

    @Override
    public void removeBookingByCancelRequest(RequestsType cancelInitiator, int requestId) {
        log.debug(" method removeBookingByCancelRequest");
        String stringRequestId = String.valueOf(requestId);
        List<Booking> bookingList;
        if (cancelInitiator.equals(RequestsType.FIND_PASSENGER_REQUEST)) {
//            bookingList =
            repository.findByFindPassRequestRedis_RequestId(stringRequestId).ifPresent(booking -> {
                Optional<BookingTemp> bookingCash = bookingTempService.findById(booking.getId());
                bookingCash.ifPresent(bc -> bc.setCanceledBy(cancelInitiator));
                deleteBooking(booking);
            });

        } else {
            bookingList = repository.findByFindRideRequestRedis_RequestId(stringRequestId);
        }
//        bookingList.forEach(booking -> {
//            Optional<BookingTemp> bookingCash = bookingTempService.findById(booking.getId());
//            bookingCash.ifPresent(bc -> bc.setCanceledBy(cancelInitiator));
//            repository.deleteById(booking.getId());
//        });
    }

    @Override
    public boolean hasBooking(RequestsType initiator, int requestId) {
        String id = String.valueOf(requestId);
        if (initiator == FIND_PASSENGER_REQUEST) {
            return repository.existsByFindPassRequestRedis_RequestId(id);
        } else if (initiator == FIND_RIDE_REQUEST) {
            return repository.existsByFindRideRequestRedis_RequestId(id);
        } else {
            throw new IllegalArgumentException("Invalid RequestsType value: " + initiator);
        }
    }

    private void increaseSeatsQuantity(Booking booking) {
        FindPassRequestRedis findPassRequestRedis = booking.getFindPassRequestRedis();
        int passengersSeatsQuantity = -booking.getFindRideRequestRedis().getPassengersQuantity();
        findPassRequestRedisService.updateSeatsQuantity(findPassRequestRedis, passengersSeatsQuantity);
    }

    private void reduceSeatsQuantity(Booking booking) {
        FindPassRequestRedis findPassRequestRedis = booking.getFindPassRequestRedis();
        int passengersSeatsQuantity = booking.getFindRideRequestRedis().getPassengersQuantity();
        findPassRequestRedisService.updateSeatsQuantity(findPassRequestRedis, passengersSeatsQuantity);
    }

}
