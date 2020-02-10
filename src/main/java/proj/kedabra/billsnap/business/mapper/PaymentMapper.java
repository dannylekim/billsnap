package proj.kedabra.billsnap.business.mapper;

import org.mapstruct.Mapper;

import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.model.projections.PaymentOwed;
import proj.kedabra.billsnap.presentation.resources.PaymentOwedResource;

@Mapper
public interface PaymentMapper {

    PaymentOwedResource toResource(PaymentOwedDTO paymentOwedDTO);

    PaymentOwedDTO toDTO(PaymentOwed paymentOwed);

}
