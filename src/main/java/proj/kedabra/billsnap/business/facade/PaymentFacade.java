package proj.kedabra.billsnap.business.facade;

import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;

public interface PaymentFacade {

    PaymentOwedDTO getAmountsOwed(final String email);

}
