package by.ivam.fellowtravelerbot.redis.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RedisRepositoryImpl implements RedisRepository {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final String KEY = "RideDto:%s:%d";
//    @Autowired
//    private HashOperations  hashOperations;
//    private HashOperations <String, Object, Object> hashOperations; // = redisTemplate.opsForHash();


//    public RedisRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }

//     private void init() {
//        hashOperations = redisTemplate.opsForHash();
//    }


//    public void add(final RideDto ride) {
////        redisTemplate.opsForHash().put(KEY, ride.getId(), ride.getDepartureAt());
//    }

//    public void addRide(RideDto ride) {
//        HashMap<String, String> rideMap = new HashMap<>();
//        rideMap.put("id", Integer.toString(ride.getId()));
//        rideMap.put("direction", ride.getDirection());
////        rideMap.put("departureAt", ride.getDepartureAt().toString());
//        rideMap.put("seatsQuantity", Integer.toString(ride.getSeatsQuantity()));
//        String key = String.format(KEY, ride.getDirection(), ride.getId());
//        redisTemplate.opsForHash().put(key, ride.getId(), rideMap);
//        hashOperations.put(key, ride.getId(), rideMap);

//        Duration expireDuration = Duration.ofSeconds(ride.getDuration());
//                // Duration.ofSeconds(LocalDateTime.now().until(ride.getDepartureAt(), ChronoUnit.SECONDS));
//        redisTemplate.expire(key, expireDuration);
//    }

//    public void delete(String id) {
//        redisTemplate.opsForHash().delete(KEY, id);
//    }
//
//    public RideDto findRide(String id) {
//
//        return (RideDto) redisTemplate.opsForHash().get(KEY, id);
//    }
//
//    public Map<Object, Object> findAllRides() {
//        return redisTemplate.opsForHash().entries(KEY);
//    }
}