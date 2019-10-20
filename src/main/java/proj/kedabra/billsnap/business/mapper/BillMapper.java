package proj.kedabra.billsnap.business.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.presentation.resources.BillCreationResource;
import proj.kedabra.billsnap.presentation.resources.BillResource;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = AccountMapper.class)
public interface BillMapper {

    Bill toEntity(BillDTO billDTO);

    BillCompleteDTO toDTO(Bill bill);

    BillDTO toDTO(BillCreationResource billCreationResource);

    BillResource toResource(BillCompleteDTO billDTO);
}
