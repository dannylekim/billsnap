package proj.kedabra.billsnap.business.facade.impl;

import org.springframework.stereotype.Service;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.facade.PaymentFacade;

@Service
public class PaymentFacadeImpl implements PaymentFacade {

    @Override
    public PaymentOwedDTO getAmountsOwed(String email) {
        // TODO implementation
        return null;
    }
}
