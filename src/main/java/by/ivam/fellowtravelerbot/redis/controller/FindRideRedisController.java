package by.ivam.fellowtravelerbot.redis.controller;

import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import by.ivam.fellowtravelerbot.redis.service.FindRideRequestRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/findride")
public class FindRideRedisController {
    @Autowired
    FindRideRequestRedisService service;


    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Iterable<FindRideRequestRedis> getAllRequests() {
        return service.findAll();
    }
    @GetMapping("/exp")
    @ResponseStatus(HttpStatus.OK)
    public List<FindRideRequestRedis> getNotExpiredRequests() {
        return service.findAllNotExpired();
    }

    @GetMapping("{id}")
    public FindRideRequestRedis findById(@PathVariable String id) {
        return service.findById(id);
    }
}
