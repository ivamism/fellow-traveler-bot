package by.ivam.fellowtravelerbot.redis.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Data
public class MatchServiceImpl implements MatchService {

    @Autowired
    FindPassRequestRedisService findPassRequestRedisService;

    @Override
    public void cancelRequestById(int id) {

    }

    @Override
    public void getNewFindPassengerRequest(int requestId) {

    }

    @Override
    public void getNewFindRideRequest(int requestId) {

    }
}
