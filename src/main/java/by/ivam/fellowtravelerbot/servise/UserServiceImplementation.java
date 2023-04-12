package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.User;
import by.ivam.fellowtravelerbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImplementation implements UserService{
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
    public void registerNewUser(User user) {

    }

    @Override
    public void deleteUser(long chatId) {

    }
}
