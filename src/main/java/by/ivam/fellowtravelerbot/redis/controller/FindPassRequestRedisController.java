package by.ivam.fellowtravelerbot.redis.controller;

import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.service.FindPassRequestRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/findpass")
public class FindPassRequestRedisController {


    @Autowired
    FindPassRequestRedisService service;

//    @PostMapping()
//    @ResponseStatus(HttpStatus.CREATED)
//    public void create(@RequestBody Dto dto) {
//        service.addRide(dto);
//    }



    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Iterable<FindPassRequestRedis> getAllRequests() {
        return service.findAll();
    }
    @GetMapping("/exp")
    @ResponseStatus(HttpStatus.OK)
    public List<FindPassRequestRedis> getNotExpiredRequests() {
        return service.findAllNotExpired();
    }

    @GetMapping("/{id}")
    public FindPassRequestRedis findById(@PathVariable String id) {
        return service.findById(id);
    }

//    @GetMapping("direction/{direction}")
//    public List<FindPassRequestRedis> findByName(@PathVariable String direction) {
//        return service.findAllByDirection(direction);
//    }
//
//    @DeleteMapping("delete/{id}")
//    public void delete(@PathVariable String id) {
//        service.delete(id);
//    }
}