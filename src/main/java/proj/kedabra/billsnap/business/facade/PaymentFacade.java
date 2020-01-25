package proj.kedabra.billsnap.business.facade;

import java.math.BigDecimal;
import java.util.List;

import proj.kedabra.billsnap.business.dto.PaymentInformationDTO;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;

public interface PaymentFacade {

    List<PaymentOwedDTO> getAmountsOwed(final String email);

    BigDecimal payBill(final PaymentInformationDTO payment);
}
