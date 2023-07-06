package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.DeletedUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedUserRepository extends JpaRepository<DeletedUser, Long> {
}