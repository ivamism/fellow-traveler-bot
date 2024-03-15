package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.repository.BookingRepository;
import by.ivam.fellowtravelerbot.servise.BookingTempService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static by.ivam.fellowtravelerbot.bot.enums.RequestsType.FIND_PASSENGER_REQUEST;


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
    public void addBooking(Pair<FindPassRequestRedis, FindRideRequestRedis> pairOfRequests, String initiator) {
        Booking booking = new Booking();
        booking.setFindPassRequestRedis(pairOfRequests.getFirst())
                .setFindRideRequestRedis(pairOfRequests.getSecond())
                .setBookedAt(LocalDateTime.now())
                .setRemindAt(LocalDateTime.now().plusMinutes(15))
                .setRemindersQuantity(0)
                .setInitiator(initiator);
        repository.save(booking);
        log.debug("addBooking: " + booking);
        reduceSeatsQuantity(booking);
        bookingTempService.saveBookingTemp(booking);

    }

    @Override
    public Booking save(Booking booking) {
        repository.save(booking);
        log.debug("save booking: " + booking);
        return booking;
    }

    @Override
    public List<Booking> findAll() {
        List<Booking> bookingList =
                StreamSupport
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

    // Increment reminds counter and set new time of reminder to accept booking
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
    public void deleteBookingDueToExpiredRequest(String requestId, RequestsType requestsType) {

    }

    @Override
    public void deleteBooking(String bookingId) {
        log.debug("delete booking: " + bookingId);
        increaseSeatsQuantity(repository.findById(bookingId).orElseThrow());
        repository.deleteById(bookingId);
    }

    @Override
    public void deleteBookings(List<Booking> bookingsToDelete) {
        log.debug("Deleting " + bookingsToDelete.size() + " bookings");
        repository.deleteAll(bookingsToDelete);
    }

    // Return true if bot still haven't sent any reminders to accept Booking
    @Override
    public boolean isNewBooking(Booking booking) {
        return booking.getRemindersQuantity() == 0;
    }

    // Check on start of application and remove any Bookings with expired TTL
    @Override
    public void removeExpired() {
        int expireDuration = -1; // this value sets by Redis to expired @TimeToLive keys
        List<Booking> expiredKeys = repository.findByExpireDuration(expireDuration);
        if (!expiredKeys.isEmpty()) {
            log.info("remove expired FindRideRequestRedis - " + expiredKeys.size());
//            expiredKeys.forEach(booking -> deleteBooking(booking));
            deleteBookings(expiredKeys);
        }
    }


    //Remove Booking if any side cancel request
    @Override
    public void removeBookingByCancelingRequest(RequestsType cancelInitiator, int requestId) {
        log.debug(" method removeBookingByCancelingRequest");
        String stringRequestId = String.valueOf(requestId);

        List<Booking> bookingList = getBookingsToDeleteOnCancelingRequest(cancelInitiator, stringRequestId);

        preDeleteActionByCancelingRequest(cancelInitiator, bookingList);
        deleteBookings(bookingList);
    }

    /*
     Performs actions before deleting Bookings by canceling the request:
     increasing seats quantity,
     set RequestType, which initiate canceling.
    */
    private void preDeleteActionByCancelingRequest(RequestsType cancelInitiator, List<Booking> bookingList) {
        bookingList.stream()
                .peek(booking -> increaseSeatsQuantity(booking))
                .map(booking -> bookingTempService.findById(booking.getId()))
                .map(bookingTemp -> bookingTemp.get())
                .peek(bookingTemp -> bookingTemp.setCanceledBy(cancelInitiator))
                .forEach(bookingTemp -> bookingTempService.saveBookingTemp(bookingTemp));
    }

    // Return List of Bookings where canceled Request was used
    private List<Booking> getBookingsToDeleteOnCancelingRequest(RequestsType cancelInitiator, String stringRequestId) {
        List<Booking> bookingList = new ArrayList<>();
        if (cancelInitiator.equals(FIND_PASSENGER_REQUEST))
            bookingList = findAll()
                    .stream()
                    .filter(booking -> booking.getFindPassRequestRedis().getRequestId().equals(stringRequestId))
                    .collect(Collectors.toList());
        else if (cancelInitiator.equals(RequestsType.FIND_RIDE_REQUEST))
            bookingList = findAll()
                    .stream()
                    .filter(booking -> booking.getFindRideRequestRedis().getRequestId().equals(stringRequestId))
                    .collect(Collectors.toList());
        return bookingList;
    }

    // Return true if Request of this type included to some Booking
    @Override
    public boolean hasBooking(RequestsType requestsType, String requestId) {
        return findAll()
                .stream()
                .anyMatch(booking -> {
                    if (requestsType == FIND_PASSENGER_REQUEST)
                        return booking.getFindPassRequestRedis().getRequestId().equals(requestId);
                    return booking.getFindRideRequestRedis().getRequestId().equals(requestId);
                });
    }

    // increase seats quantity of FindPassRequestRedis due to deletion of the Booking
    private void increaseSeatsQuantity(Booking booking) {
        int passengersSeatsQuantity = getPassengersQuantity(booking);
        changeSeatsQuantity(booking, passengersSeatsQuantity);
        log.debug("method increaseSeatsQuantity");
    }

    // reduce seats quantity in the FindPassRequestRedis due to booking
    private void reduceSeatsQuantity(Booking booking) {
        int passengersSeatsQuantity = -getPassengersQuantity(booking);
        changeSeatsQuantity(booking, passengersSeatsQuantity);
        log.debug("method reduceSeatsQuantity");
    }

    //  change seats quantity in the FindPassRequestRedis depending on the booking or deletion of the booking
    private void changeSeatsQuantity(Booking booking, int passengersSeatsQuantity) {
        Optional<FindPassRequestRedis> optionalFindPassRequestRedis = Optional.of(booking.getFindPassRequestRedis());
        optionalFindPassRequestRedis.ifPresent(findPassRequestRedis ->
                findPassRequestRedisService.updateSeatsQuantity(findPassRequestRedis, passengersSeatsQuantity));
    }

    // get passengers quantity to reduce or increase seats quantity depending on the creation or deletion of the booking
    private int getPassengersQuantity(Booking booking) {
        return Optional.ofNullable(booking.getFindRideRequestRedis())
                .map(findRideRequestRedis -> findRideRequestRedis.getPassengersQuantity()).orElse(0);
    }

}
