package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.bot.enums.BookingInitiator;
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

@Service
@Data
@Log4j
public class MatchServiceImpl implements MatchService {

    @Autowired
    FindPassRequestRedisService findPassRequestRedisService;
    @Autowired
    FindRideRequestRedisService findRideRequestRedisService;
    @Autowired
    BookingService bookingService;

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
    public void addBooking(String findPassRequestId, String findRideRequestId, String initiator) {
        FindPassRequestRedis findPassRequestRedis = findPassRequestRedisService.findById(findPassRequestId);
        FindRideRequestRedis findRideRequestRedis = findRideRequestRedisService.findById(findRideRequestId);
        Booking booking = new Booking();
        booking.setFindPassRequestRedis(findPassRequestRedis)
                .setFindRideRequestRedis(findRideRequestRedis)
                .setBookedAt(LocalDateTime.now())
//                .setRemindAt(LocalDateTime.now().plusMinutes(15))
                .setRemindAt(LocalDateTime.now().plusMinutes(1))
                .setRemindersQuantity(0);
        if (initiator.equals(BookingInitiator.FIND_PASSENGER_REQUEST.getValue())) {
            booking.setInitiator(BookingInitiator.FIND_PASSENGER_REQUEST.getValue())
                    .setExpireDuration(findPassRequestRedis.getExpireDuration());
        } else {
            booking.setInitiator(BookingInitiator.FIND_RIDE_REQUEST.getValue())
                    .setExpireDuration(findRideRequestRedis.getExpireDuration());
        }
        bookingService.save(booking);
        log.debug("method addBooking" + booking);
    }

}
