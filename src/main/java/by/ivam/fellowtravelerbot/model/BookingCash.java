package by.ivam.fellowtravelerbot.model;

import by.ivam.fellowtravelerbot.bot.enums.RequestsType;
import by.ivam.fellowtravelerbot.redis.model.FindPassRequestRedis;
import by.ivam.fellowtravelerbot.redis.model.FindRideRequestRedis;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
public class BookingCash {
    @Id
    @Column(name = "id")
    private String id;
    int findPassengerRequestId;
    int findRideRequestId;
    private RequestsType bookingInitiator;
    private RequestsType cancelInitiator;
    private LocalDateTime bookedAt;
    private LocalDateTime expireAt;
}
