package proj.kedabra.billsnap.business.facade;

import proj.kedabra.billsnap.business.dto.PaymentsOwedDTO;
import proj.kedabra.billsnap.business.entities.Account;

public interface PaymentsFacade {

    PaymentsOwedDTO getAmountsOwed(final String email);

}
