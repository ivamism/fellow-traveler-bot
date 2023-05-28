package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.RegUser;
import by.ivam.fellowtravelerbot.model.User;
import by.ivam.fellowtravelerbot.repository.UserRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Log4j
public class UserServiceImplementation implements UserService {
    @Autowired
    private UserRepository userRepository;


    @Override
    public User findUserById(long chatId) {
        return null;
    }

    @Override
    public Optional<User> findById(long chatId) {

        return userRepository.findById(chatId);
    }

    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public void registerNewUser(RegUser regUser) {
        User user = new User();
        user.setChatId(regUser.getChatId())
                .setFirstName(regUser.getFirstName())
                .setUserName(regUser.getTelegramUserName())
                .setRegisteredAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("User: " + user + " saved to DB");
    }


    @Override
    public void deleteUser(long chatId) {

    }
}
