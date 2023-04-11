package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.User;

import java.util.List;

public interface UserService {

    User findById();
    List <User> findAll();

    void registerNewUser();

    void deleteUser();

}
