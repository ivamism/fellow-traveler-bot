package by.ivam.fellowtravelerbot.servise;

import by.ivam.fellowtravelerbot.model.Settlement;

import java.util.List;

public interface SettlementService {
   Settlement findById(int id);
   Settlement findByName(String name);
   List<Settlement> findAll();
   List<Settlement> findAllExcept(String name);
   List<Settlement> findAllExcept(String name, String name1);

   Settlement addNewSettlement(String settlementName);
   Settlement updateSettlement(Settlement settlement);
   void deleteById(int id);

}
