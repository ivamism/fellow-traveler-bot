package by.ivam.fellowtravelerbot.commandLineRunner;

import by.ivam.fellowtravelerbot.redis.model.Booking;
import by.ivam.fellowtravelerbot.redis.service.BookingService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(4)
@Log4j
public class OrphanBookingRemover implements CommandLineRunner {
    @Autowired
    BookingService bookingService;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.debug("Dis-activation of expired requests");
            List<Booking> bookingsToDelete = bookingService.findAll()
                    .stream()
                    .filter(booking -> booking.getFindPassRequestRedis() == null || booking.getFindRideRequestRedis() == null)
                    .collect(Collectors.toList());
            if (bookingsToDelete.isEmpty()) {
                log.info("No orphan bookings to delete");
            } else bookingService.deleteBookings(bookingsToDelete);

        } catch (Exception e) {
            log.error("An error occurred while dis-activating expired requests", e);
        }
    }
}
