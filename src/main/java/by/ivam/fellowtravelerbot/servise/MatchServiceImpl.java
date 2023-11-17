package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.service.FindPassRequestRedisService;
import by.ivam.fellowtravelerbot.redis.service.FindRideRequestRedisService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
public class MatchServiceImpl implements MatchService {

    @Autowired
    FindPassRequestRedisService findPassRequestRedisService;
    @Autowired
    FindRideRequestRedisService findRideRequestRedisService;

    @Override
    public void getNewFindPassengerRequest(String requestId) {
        FindPassRequestRedis receivedRequest = findPassRequestRedisService.findById(requestId);
        List<Integer> suitableRequestIdList = findRideRequestRedisService.findAllByDirection(receivedRequest.getDirection())
                .stream()
                .filter(request -> request.getDepartureBefore().isBefore(receivedRequest.getDepartureAt()))
                .filter(request -> request.getDepartureBefore().toLocalDate().isEqual(receivedRequest.getDepartureAt().toLocalDate()))
                .filter(request -> request.getPassengersQuantity() <= receivedRequest.getSeatsQuantity())
                .map(request -> Integer.parseInt(request.getRequestId()))
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
    }

    @Override
    public void addBooking(Booking booking) {

    }


    private boolean isSuitableTime( LocalDateTime requestTime, LocalDateTime timeToCompare){
        return requestTime.isBefore(timeToCompare)&&requestTime.toLocalDate().isEqual(timeToCompare.toLocalDate());
    }
}
