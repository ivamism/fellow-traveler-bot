package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Settlement;
import by.ivam.fellowtravelerbot.repository.SettlementRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Log4j
public class SettlementServiceImplementation implements SettlementService {
//    @Autowired
//    Settlement settlement;
    @Autowired
    SettlementRepository settlementRepository;

    @Override
    public Settlement findById(int id) {
        return settlementRepository.findById(id).orElseThrow();

    }

    @Override
    public Settlement findByName(String name) {
        log.debug("findById Settlement by name");
        return settlementRepository.findByName(name).orElseThrow();
    }

    @Override
    public List<Settlement> findAll() {
        log.debug("get list of Settlements from DB");
        return settlementRepository.findAll();
    }

    @Override
    public List<Settlement> findAllExcept(String name) {
        return settlementRepository.findByNameNotLike(name);
    }

    @Override
    public List<Settlement> findAllExcept(String name, String name1) {
        return settlementRepository.findByNameNotLikeIgnoreCaseAndNameNotLikeIgnoreCase(name, name1);
    }

    @Override
    public Settlement addNewSettlement(String settlementName) {
        Settlement settlement = new Settlement();
        settlement.setName(settlementName);
        settlementRepository.save(settlement);
        log.info("Settlement " + settlement + " saved to DB");
        return settlement;
    }

    @Override
    public Settlement updateSettlement(Settlement settlement) {
        log.info("Update settlement: " + settlement);
        return settlementRepository.save(settlement);
    }

    @Override
    public void deleteById(int id) {

    }
}
