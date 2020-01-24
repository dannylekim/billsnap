package proj.kedabra.billsnap.business.facade;

import java.util.List;

import proj.kedabra.billsnap.business.dto.PaymentDTO;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.dto.RemainingAmountDTO;

public interface PaymentFacade {

    List<PaymentOwedDTO> getAmountsOwed(final String email);

    RemainingAmountDTO payBill(final PaymentDTO payment);
}
