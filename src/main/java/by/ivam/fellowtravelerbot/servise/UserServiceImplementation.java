package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.User;
import by.ivam.fellowtravelerbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImplementation implements UserService{
    @Autowired
   private UserRepository userRepository;


    @Override
    public User findById() {
        return null;
    }

    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public void registerNewUser() {

    }

    @Override
    public void deleteUser() {

    }
}
