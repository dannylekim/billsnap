package proj.kedabra.billsnap.business.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.EditBillDTO;
import proj.kedabra.billsnap.business.dto.PendingRegisteredBillSplitDTO;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.presentation.resources.AssociateBillResource;
import proj.kedabra.billsnap.presentation.resources.BillCreationResource;
import proj.kedabra.billsnap.presentation.resources.BillResource;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;
import proj.kedabra.billsnap.presentation.resources.EditBillResource;
import proj.kedabra.billsnap.presentation.resources.PendingRegisteredBillSplitResource;
import proj.kedabra.billsnap.presentation.resources.ShortBillResource;

@Mapper(uses = {AccountMapper.class, ItemMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR, unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface BillMapper {

    Bill toEntity(BillDTO billDTO);

    BillCompleteDTO toBillCompleteDTO(Bill bill);

    BillDTO toBillDTO(BillCreationResource billCreationResource);

    BillResource toResource(BillCompleteDTO billDTO);

    ShortBillResource toShortBillResource(BillSplitDTO billSplitDTO);

    BillSplitResource toResource(BillSplitDTO billSplitDTO);

    PendingRegisteredBillSplitResource toResource(PendingRegisteredBillSplitDTO pendingRegisteredBillSplitDTO);

    BillSplitDTO toBillSplitDTO(Bill bill);

    PendingRegisteredBillSplitDTO toPendingRegisteredBillSplitDTO(BillSplitDTO billSplitDTO);

    @Mapping(target = "items", source = "itemsPerAccount")
    AssociateBillDTO toAssociateBillDTO(AssociateBillResource associateBillResource);

    EditBillDTO toDTO(EditBillResource editBillResource);

    @Mapping(source = "editBillDTO.name", target = "name")
    @Mapping(source = "editBillDTO.responsible", target = "responsible")
    @Mapping(source = "editBillDTO.company", target = "company")
    @Mapping(source = "editBillDTO.category", target = "category")
    @Mapping(source = "editBillDTO.tipPercent", target = "tipPercent", ignore = true)
    @Mapping(source = "editBillDTO.tipAmount", target = "tipAmount", ignore = true)
    @Mapping(source = "editBillDTO.items", target = "items", ignore = true)
    void updatebill(@MappingTarget Bill bill, EditBillDTO editBillDTO);

}
