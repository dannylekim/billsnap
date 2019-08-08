package proj.kedabra.billsnap.business.facade;

import proj.kedabra.billsnap.business.dto.BillDTO;

public interface BillFacade {

    BillDTO addPersonalBill(String email, BillDTO billDTO);
}
