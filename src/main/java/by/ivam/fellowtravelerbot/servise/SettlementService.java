package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Settlement;

import java.util.List;

public interface SettlementService {
   Settlement findById(int id);
   List<Settlement> findAll();
   Settlement addNewSettlement(String settlementName);
   Settlement updateSettlement(Settlement settlement);
   void deleteById(int id);

}
