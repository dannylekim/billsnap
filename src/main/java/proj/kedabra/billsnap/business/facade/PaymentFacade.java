package proj.kedabra.billsnap.business.facade;

import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;

import java.util.List;

public interface PaymentFacade {

    List<PaymentOwedDTO> getAmountsOwed(final String email);

}
