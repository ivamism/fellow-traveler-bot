package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}