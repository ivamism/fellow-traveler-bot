package by.ivam.fellowtravelerbot.redis.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@RedisHash("find_ride_request")
@Accessors(chain = true)
public class FindRideRequestRedis {
    @Id
    private String requestId;

    private long chatId;

    @Indexed
    private String direction;

    private LocalDateTime DepartureBefore;

    private int passengersQuantity;

    @TimeToLive
    private long expireDuration;
}
