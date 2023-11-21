package by.ivam.fellowtravelerbot.commandLineRunner;

import by.ivam.fellowtravelerbot.servise.FindPassengerRequestService;
import by.ivam.fellowtravelerbot.servise.FindRideRequestService;
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
    FindPassengerRequestService findPassengerRequestService;
    @Autowired
    FindRideRequestService findRideRequestService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Dis-activation of expired requests");

    }
}
