package proj.kedabra.billsnap.business.facade;

import proj.kedabra.billsnap.business.dto.PaymentsOwedDTO;

public interface PaymentFacade {

    PaymentsOwedDTO getAmountsOwed(final String email);

}
