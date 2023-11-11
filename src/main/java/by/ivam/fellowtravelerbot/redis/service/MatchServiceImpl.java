package by.ivam.fellowtravelerbot.redis.service;

import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.servise.handler.FindPassengerHandler;
import by.ivam.fellowtravelerbot.servise.handler.FindRideHandler;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Data
public class MatchServiceImpl implements MatchService {

    @Autowired
    FindPassRequestRedisService findPassRequestRedisService;
    @Autowired
    FindRideRequestRedisService findRideRequestRedisService;

    @Autowired
    FindRideHandler findRideHandler;
    @Autowired
    FindPassengerHandler findPassengerHandler;

    @Override
    public void getNewFindPassengerRequest(String requestId) {
        FindPassRequestRedis receivedRequest = findPassRequestRedisService.findById(requestId);
        long chatId = receivedRequest.getChatId();
        String direction = receivedRequest.getDirection();
        LocalDateTime departureAt = receivedRequest.getDepartureAt();
        int seatsQuantity = receivedRequest.getSeatsQuantity();
        List<Long> matchedChatIdList = findRideRequestRedisService
                .findMatches(direction, departureAt, seatsQuantity);
//                .stream()
//                .map(request -> request.getChatId())
//                .collect(Collectors.toList());


        findPassengerHandler.sendAppearedNewPassengerRequestMessage(matchedChatIdList, Integer.parseInt(requestId));
    }



    @Override
    public void getNewFindRideRequest(String requestId) {
        FindRideRequestRedis receivedRequest = findRideRequestRedisService.findById(requestId);
        long chatId = receivedRequest.getChatId();
        String direction = receivedRequest.getDirection();
        LocalDateTime departureBefore = receivedRequest.getDepartureBefore();
        int passengersQuantity = receivedRequest.getPassengersQuantity();
        List<Long> matchedChatIdList = getFindPassRequestRedisService()
                .findMatches(direction, departureBefore, passengersQuantity);
//                .stream()
//                .map(request -> request.getChatId())
//                .collect(Collectors.toList());
findRideHandler.sendAppearedNewFindRideRequestMessage(matchedChatIdList, Integer.parseInt(requestId));
    }
}
