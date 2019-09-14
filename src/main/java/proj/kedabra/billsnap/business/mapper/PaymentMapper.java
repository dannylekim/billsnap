package proj.kedabra.billsnap.buserusiness.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import proj.kedabra.billsnap.business.dto.PaymentsOwedDTO;
import proj.kedabra.billsnap.presentation.resources.PaymentsOwedRessource;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PaymentMapper {

    PaymentsOwedRessource toRessource(PaymentsOwedDTO paymentsOwedDTO);

}
