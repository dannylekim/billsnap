package proj.kedabra.billsnap.business.facade;

import java.util.List;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.PendingRegisteredBillSplitDTO;

public interface BillFacade {

    BillCompleteDTO addPersonalBill(String email, BillDTO billDTO);

    List<BillSplitDTO> getAllBillsByEmail(String email);

    BillSplitDTO getDetailedBillByEmail(String email, Long billId);

    BillSplitDTO associateAccountsToBill(AssociateBillDTO associateBillDTO);

    PendingRegisteredBillSplitDTO inviteRegisteredToBill(Long billId, String userEmail, List<String> accounts);

}
