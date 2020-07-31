package proj.kedabra.billsnap.business.facade;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.EditBillDTO;
import proj.kedabra.billsnap.business.dto.GetBillPaginationDTO;
import proj.kedabra.billsnap.business.model.entities.Bill;

public interface BillFacade {

    BillCompleteDTO addPersonalBill(String email, BillDTO billDTO);

    @Transactional(readOnly = true)
    List<BillSplitDTO> getAllBillsByEmailPageable(GetBillPaginationDTO dto);

    BillSplitDTO associateAccountsToBill(AssociateBillDTO associateBillDTO);

    BillSplitDTO inviteRegisteredToBill(Long billId, List<String> accounts);

    BillSplitDTO getDetailedBill(Long billId);

    BillSplitDTO startBill(Long billId);

    BillSplitDTO getBillSplitDTO(Bill bill);

    BillSplitDTO editBill(Long billId, String email, EditBillDTO editBill);

}
