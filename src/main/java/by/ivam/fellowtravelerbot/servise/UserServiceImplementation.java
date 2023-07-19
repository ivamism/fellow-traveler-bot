package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.DTO.UserDTO;
import by.ivam.fellowtravelerbot.model.DeletedUser;
import by.ivam.fellowtravelerbot.model.User;
import by.ivam.fellowtravelerbot.repository.DeletedUserRepository;
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
    @Autowired
    private DeletedUserRepository deletedUserRepository;


    @Override
    public User findUserById(long chatId) {

        return userRepository.findById(chatId).orElseThrow();
    }

    @Override
    public Optional<User> findById(long chatId) {

        return userRepository.findById(chatId);
    }

    @Override
    public List<User> findAll() {

        return userRepository.findAll();
    }

    @Override
    public User registerNewUser(UserDTO userDTO) {
        User user = new User();
        user.setChatId(userDTO.getChatId())
                .setFirstName(userDTO.getFirstName())
                .setUserName(userDTO.getTelegramUserName())
                .setRegisteredAt(LocalDateTime.now())
                .setResidence(userDTO.getResidence());
        if (userDTO.getChatId()==785703113) user.setAdmin(true);

        userRepository.save(user);
        log.info("User: " + user + " saved to DB");
        return user;
    }


    @Override
    public void deleteUser(long chatId) {
        User user = findUserById(chatId);
        DeletedUser deletedUser = new DeletedUser();
        deletedUser.setChatId(user.getChatId())
                .setUserName(user.getUserName())
                .setRegisteredAt(user.getRegisteredAt())
                .setDeletedAt(LocalDateTime.now());
        deletedUserRepository.save(deletedUser);
        userRepository.delete(user);
        log.info("User " + user + " deleted from \"users\" and saved as \"deletedUser\"" + deletedUser);

    }

    @Override
    public void updateUserFirstName(long chatId, String firstName) {
        User user = findUserById(chatId);
        user.setFirstName(firstName);
        userRepository.save(user);
        log.info("Update User's FirstName to " + firstName);
    }
}
