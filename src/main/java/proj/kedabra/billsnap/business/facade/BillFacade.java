package proj.kedabra.billsnap.business.facade;

import java.util.List;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.EditBillDTO;
import proj.kedabra.billsnap.business.dto.PendingRegisteredBillSplitDTO;
import proj.kedabra.billsnap.business.model.entities.Bill;

public interface BillFacade {

    BillCompleteDTO addPersonalBill(String email, BillDTO billDTO);

    List<BillSplitDTO> getAllBillsByEmail(String email);

    BillSplitDTO associateAccountsToBill(AssociateBillDTO associateBillDTO, String responsibleEmail);

    PendingRegisteredBillSplitDTO inviteRegisteredToBill(Long billId, String userEmail, List<String> accounts);

    BillSplitDTO getDetailedBill(Long billId, String userEmail);

    BillSplitDTO startBill(Long billId, String userEmail);

    BillSplitDTO getBillSplitDTO(Bill bill);

    BillSplitDTO editBill(Long billId, String email, EditBillDTO editBill);

}
