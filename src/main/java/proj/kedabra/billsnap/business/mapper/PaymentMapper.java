package proj.kedabra.billsnap.business.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.entities.IPaymentOwed;
import proj.kedabra.billsnap.presentation.resources.PaymentOwedResource;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PaymentMapper {

    PaymentOwedResource toResource(PaymentOwedDTO paymentOwedDTO);

    PaymentOwedDTO toDTO(IPaymentOwed paymentOwed);

}
