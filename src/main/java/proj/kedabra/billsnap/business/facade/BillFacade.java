package proj.kedabra.billsnap.business.facade;

import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;

public interface BillFacade {

    BillCompleteDTO addPersonalBill(String email, BillDTO billDTO);
}
