package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.bot.enums.BookingInitiator;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.model.Ride;
import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.service.BookingService;
import by.ivam.fellowtravelerbot.redis.service.FindPassRequestRedisService;
import by.ivam.fellowtravelerbot.redis.service.FindRideRequestRedisService;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Data
@Log4j
public class MatchServiceImpl implements MatchService {

    @Autowired
    private FindPassRequestRedisService findPassRequestRedisService;
    @Autowired
    private FindRideRequestRedisService findRideRequestRedisService;
    @Autowired
    private FindPassengerRequestService findPassengerRequestService;
    @Autowired
    private FindRideRequestService findRideRequestService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private RideService rideService;

    @Override
    public void getNewFindPassengerRequest(String requestId) {
        FindPassRequestRedis receivedRequest = findPassRequestRedisService.findById(requestId);
        List<Integer> suitableRequestIdList = findRideRequestRedisService.findAllByDirection(receivedRequest.getDirection())
                .stream()
                .filter(request -> request.getDepartureBefore().isBefore(receivedRequest.getDepartureAt()))
                .filter(request -> request.getDepartureBefore().toLocalDate().isEqual(receivedRequest.getDepartureAt().toLocalDate()))
                .filter(request -> request.getPassengersQuantity() <= receivedRequest.getSeatsQuantity())
                .map(request -> Integer.parseInt(request.getRequestId()))
                .toList();
        log.debug("method getNewFindPassengerRequest. Found matches: " + suitableRequestIdList.size());
    }

    @Override
    public void getNewFindRideRequest(String requestId) {
        FindRideRequestRedis receivedRequest = findRideRequestRedisService.findById(requestId);
        List<Integer> suitableRequestIdList = findPassRequestRedisService.findAllByDirection(receivedRequest.getDirection())
                .stream()
                .filter(request -> request.getDepartureAt().toLocalDate().isEqual(receivedRequest.getDepartureBefore().toLocalDate()))
                .filter(request -> request.getDepartureAt().isBefore(receivedRequest.getDepartureBefore()))
                .filter(request -> request.getSeatsQuantity() >= receivedRequest.getPassengersQuantity())
                .map(request -> Integer.parseInt(request.getRequestId()))
                .toList();
        log.debug("method getNewFindRideRequest. Found matches: " + suitableRequestIdList.size());
    }

    @Override
    public void addBooking(String firstId, String secondId, String initiator) {
        FindPassRequestRedis findPassRequestRedis;
        FindRideRequestRedis findRideRequestRedis;
        Booking booking = new Booking();
        booking.setBookedAt(LocalDateTime.now())
                .setRemindAt(LocalDateTime.now().plusMinutes(10))
                .setRemindersQuantity(0);
        if (initiator.equals(BookingInitiator.FIND_PASSENGER_REQUEST.getValue())) {
            findPassRequestRedis = findPassRequestRedisService.findById(firstId);
            findRideRequestRedis = findRideRequestRedisService.findById(secondId);
            booking.setFindPassRequestRedis(findPassRequestRedis)
                    .setFindRideRequestRedis(findRideRequestRedis)
                    .setInitiator(BookingInitiator.FIND_PASSENGER_REQUEST.getValue())
                    .setExpireDuration(findPassRequestRedis.getExpireDuration());

        } else {
            findRideRequestRedis = findRideRequestRedisService.findById(firstId);
            findPassRequestRedis = findPassRequestRedisService.findById(secondId);
            booking.setFindPassRequestRedis(findPassRequestRedis)
                    .setFindRideRequestRedis(findRideRequestRedis)
                    .setInitiator(BookingInitiator.FIND_RIDE_REQUEST.getValue())
                    .setExpireDuration(findRideRequestRedis.getExpireDuration());
        }
        bookingService.save(booking);
        int passengersQuantity = findRideRequestRedis.getPassengersQuantity();
        findPassRequestRedisService.updateSeatsQuantity(findPassRequestRedis, passengersQuantity);

        log.debug("method addBooking: " + booking);
    }

    @Override
    public List<Integer> getFindPassRequestMatches(FindRideRequestRedis request) {
        return findPassRequestRedisService.findMatches(request);
    }

    @Override
    public List<Integer> getFindRideRequestMatches(FindPassRequestRedis request) {
        return findRideRequestRedisService.findMatches(request);
    }

    @Override
    public void deleteBooking(String bookingId) {
        bookingService.deleteBooking(bookingId);
    }

    @Override
    public Booking getBooking(String bookingId) {
        return bookingService.findById(bookingId);
    }

    @Override
    public Ride createOrUpdateRide(String bookingId) {
        Booking booking = bookingService.findById(bookingId);
        int findPassengerRequestId = Integer.parseInt(booking.getFindPassRequestRedis().getRequestId());
        int findRideRequestId = Integer.parseInt(booking.getFindRideRequestRedis().getRequestId());
        Optional<Ride> optionalRide = rideService.getRideByFindPassengerRequestId(findPassengerRequestId);
        Ride ride;
        if (optionalRide.isPresent()) {
            ride = optionalRide.get();
            Set<FindRideRequest> findRideRequests = ride.getFindRideRequests();
            findRideRequests.add(findRideRequestService.findById(findPassengerRequestId));
        } else {
            ride = createNewRide(findPassengerRequestId, findRideRequestId);
        }
        return rideService.saveRide(ride);
    }

    private Ride createNewRide(int findPassengerRequestId, int findRideRequestId) {
        return rideService.createNewRide(findPassengerRequestId, findRideRequestId);
    }

}
