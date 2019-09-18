package proj.kedabra.billsnap.business.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.presentation.resources.BillCreationResource;
import proj.kedabra.billsnap.presentation.resources.BillResource;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = AccountMapper.class)
public interface BillMapper {

    Bill toEntity(BillDTO billDTO);

    BillCompleteDTO toBillCompleteDTO(Bill bill);

    BillDTO toBillDTO(BillCreationResource billCreationResource);

    BillResource toResource(BillCompleteDTO billDTO);

    BillSplitDTO toBillSplitDTO(Bill bill);
}
