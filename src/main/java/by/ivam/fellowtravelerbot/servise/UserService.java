package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User findUserById(long chatId);
    Optional<User> findById(long chatId);

    List <User> findAll();

    void registerNewUser(User user);

    void deleteUser(long chatId);

}
