package by.ivam.fellowtravelerbot.repository;

import by.ivam.fellowtravelerbot.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    List<Location> findAllBySettlement_Id(int id);

}