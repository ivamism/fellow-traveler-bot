package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}