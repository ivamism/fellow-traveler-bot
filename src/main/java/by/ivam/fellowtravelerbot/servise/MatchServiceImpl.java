package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.FindPassengerRequest;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.service.FindPassRequestRedisService;
import by.ivam.fellowtravelerbot.redis.service.FindRideRequestRedisService;
import by.ivam.fellowtravelerbot.servise.handler.MatchingHandler;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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

    @Autowired
    MatchingHandler matchingHandler;

//    @Autowired
//    FindRideHandler findRideHandler;
//    @Autowired
//    FindPassengerHandler findPassengerHandler;

    @Override
    public void getNewFindPassengerRequest(String requestId) {
        FindPassRequestRedis receivedRequest = findPassRequestRedisService.findById(requestId);
        List<Integer> suitableRequestIdList = findRideRequestRedisService.findAllByDirection(receivedRequest.getDirection())
                .stream()
//                .filter(request -> isSuitableTime(receivedRequest.getDepartureAt(), request.getDepartureBefore()))
                .filter(request -> request.getDepartureBefore().isBefore(receivedRequest.getDepartureAt()))
                .filter(request -> request.getDepartureBefore().toLocalDate().isEqual(receivedRequest.getDepartureAt().toLocalDate()))
                .filter(request -> request.getPassengersQuantity() <= receivedRequest.getSeatsQuantity())
                .map(request -> Integer.parseInt(request.getRequestId()))
                .collect(Collectors.toList());
        matchingHandler.sendListOfSuitableFindRideRequestMessage(suitableRequestIdList, receivedRequest.getChatId());
//        long chatId = receivedRequest.getChatId();
//        String direction = receivedRequest.getDirection();
//        LocalDateTime departureAt = receivedRequest.getDepartureAt();
//        int seatsQuantity = receivedRequest.getSeatsQuantity();
//        List<Long> matchedChatIdList = findRideRequestRedisService
//                .findMatches(direction, departureAt, seatsQuantity);
//        findRideHandler.sendAppearedNewFindRideRequestMessage(matchedChatIdList, Integer.parseInt(requestId));
    }

    @Async
    @Override
    public void getNewFindPassengerRequest(FindPassengerRequest receivedRequest) {
//        FindPassRequestRedis receivedRequest = findPassRequestRedisService.findById(requestId);
//        long chatId = receivedRequest.getChatId();
//        String direction = receivedRequest.getDirection();

        List<Integer> suitableRequestIdList = findRideRequestRedisService.findAllByDirection(receivedRequest.getDirection())
                .stream()
                .filter(request -> request.getDepartureBefore().isBefore(receivedRequest.getDepartureAt()))
//                .filter(request -> isSuitableTime(receivedRequest.getDepartureAt(), request.getDepartureBefore()))
                .filter(request -> request.getPassengersQuantity() <= receivedRequest.getSeatsQuantity())
                .map(request -> Integer.parseInt(request.getRequestId()))
                .collect(Collectors.toList());
        matchingHandler.sendListOfSuitableFindRideRequestMessage(suitableRequestIdList, receivedRequest.getUser().getChatId());
//        LocalDateTime departureAt = receivedRequest.getDepartureAt();
//        int seatsQuantity = receivedRequest.getSeatsQuantity();
//        List<Long> matchedChatIdList = findRideRequestRedisService
//                .findMatches(direction, departureAt, seatsQuantity);
//        findRideHandler.sendAppearedNewFindRideRequestMessage(matchedChatIdList, Integer.parseInt(requestId));
    }


    @Override
    public void getNewFindRideRequest(String requestId) {
        FindRideRequestRedis receivedRequest = findRideRequestRedisService.findById(requestId);
        List<Integer> suitableRequestIdList = findPassRequestRedisService.findAllByDirection(receivedRequest.getDirection())
                .stream()
//                .filter(request -> isSuitableTime(receivedRequest.getDepartureBefore(), request.getDepartureAt()))
                .filter(request -> request.getDepartureAt().toLocalDate().isEqual(receivedRequest.getDepartureBefore().toLocalDate()))

                .filter(request -> request.getDepartureAt().isBefore(receivedRequest.getDepartureBefore()))
                .filter(request -> request.getSeatsQuantity() >= receivedRequest.getPassengersQuantity())
                .map(request -> Integer.parseInt(request.getRequestId()))
                .collect(Collectors.toList());
        matchingHandler.sendListOfSuitableFindPassengerRequestMessage(suitableRequestIdList, receivedRequest.getChatId());

//        long chatId = receivedRequest.getChatId();
//        String direction = receivedRequest.getDirection();
//        LocalDateTime departureBefore = receivedRequest.getDepartureBefore();
//        int passengersQuantity = receivedRequest.getPassengersQuantity();
//        List<Long> matchedChatIdList = getFindPassRequestRedisService()
//                .findMatches(direction, departureBefore, passengersQuantity);
//        findPassengerHandler.sendAppearedNewPassengerRequestMessage(matchedChatIdList, Integer.parseInt(requestId));
    }

    @Async
    @Override
    public void getNewFindRideRequest(FindRideRequest receivedRequest) {
        List<Integer> suitableRequestIdList = findPassRequestRedisService.findAllByDirection(receivedRequest.getDirection())
                .stream()
                .filter(request -> isSuitableTime(receivedRequest.getDepartureBefore(), request.getDepartureAt()))
//                .filter(request -> request.getDepartureAt().isBefore(receivedRequest.getDepartureBefore()))
                .filter(request -> request.getSeatsQuantity() >= receivedRequest.getPassengersQuantity())
                .map(request -> Integer.parseInt(request.getRequestId()))
                .collect(Collectors.toList());
        matchingHandler.sendListOfSuitableFindPassengerRequestMessage(suitableRequestIdList, receivedRequest.getUser().getChatId());
    }

    private boolean isSuitableTime( LocalDateTime requestTime, LocalDateTime timeToCompare){
        return requestTime.isBefore(timeToCompare)&&requestTime.toLocalDate().isEqual(timeToCompare.toLocalDate());
    }
}
