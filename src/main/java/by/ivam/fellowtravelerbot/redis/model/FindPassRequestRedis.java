package by.ivam.fellowtravelerbot.redis.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@RedisHash("find_passenger_request")
@Accessors(chain = true)
public class FindPassRequestRedis {
    @Id
    String requestId;
    long chatId;
    @Indexed
    String direction;
    LocalDateTime DepartureAt;
    int seatsQuantity;
    @TimeToLive
    long expireDuration;
}
