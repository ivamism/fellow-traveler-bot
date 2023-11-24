package by.ivam.fellowtravelerbot.commandLineRunner;

import by.ivam.fellowtravelerbot.redis.service.BookingService;
import by.ivam.fellowtravelerbot.redis.service.FindPassRequestRedisService;
import by.ivam.fellowtravelerbot.redis.service.FindRideRequestRedisService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@Log4j
public class ExpiredRedisHashesRemover implements CommandLineRunner {
    @Autowired
    FindPassRequestRedisService findPassRequestRedisService;
    @Autowired
    FindRideRequestRedisService findRideRequestRedisService;

    @Autowired
    BookingService bookingService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Dis-activation of expired requests");
        bookingService.removeExpired();
        findPassRequestRedisService.removeExpired();
        findRideRequestRedisService.removeExpired();
    }
}
