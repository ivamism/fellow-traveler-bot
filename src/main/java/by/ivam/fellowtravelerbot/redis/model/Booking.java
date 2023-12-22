package by.ivam.fellowtravelerbot.redis.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.LocalDateTime;

@Data
@RedisHash("booking")
@Accessors(chain = true)
public class Booking {
    @Id
    private String id;

    @Reference
    private FindPassRequestRedis findPassRequestRedis;

    @Reference
    private FindRideRequestRedis findRideRequestRedis;

    private String initiator;  // TODO изменить на Enum RequestsType

    private LocalDateTime bookedAt;

    private LocalDateTime remindAt;

    private int remindersQuantity;

    @TimeToLive
    private long expireDuration;
}
