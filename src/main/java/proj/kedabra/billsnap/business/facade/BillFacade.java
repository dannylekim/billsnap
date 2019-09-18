package proj.kedabra.billsnap.business.facade;

import java.util.List;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;

public interface BillFacade {

    BillCompleteDTO addPersonalBill(String email, BillDTO billDTO);

    List<BillCompleteDTO> getAllBillsByEmail(String email);

    BillSplitDTO associateAccountsToBill(AssociateBillDTO associateBillDTO);
}
