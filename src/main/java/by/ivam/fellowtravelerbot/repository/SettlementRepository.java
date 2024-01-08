package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Integer> {
    Optional<Settlement> findByName(String name);

    List<Settlement> findByNameNotLike(String name);

    List<Settlement> findByNameNotLikeIgnoreCaseAndNameNotLikeIgnoreCase(String name, String name1);

}