package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Integer> {
}