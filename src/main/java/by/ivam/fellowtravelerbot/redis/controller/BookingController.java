package by.ivam.fellowtravelerbot.redis.controller;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {
    @Autowired
    BookingService service;


    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<Booking> getAllRequests() {
        return service.findAll();
    }
//    @GetMapping("/exp")
//    @ResponseStatus(HttpStatus.OK)
//    public List<FindRideRequestRedis> getNotExpiredRequests() {
//        return service.findAllNotExpired();
//    }

    @GetMapping("{id}")
    public Booking findById(@PathVariable String id) {
        return service.findById(id);
    }
}
