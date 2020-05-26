package proj.kedabra.billsnap.business.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

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
import proj.kedabra.billsnap.presentation.resources.PendingRegisteredBillSplitResource;
import proj.kedabra.billsnap.presentation.resources.ShortBillResource;

@Mapper(uses = AccountMapper.class)
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

    default void editBillToBill(Bill bill, EditBillDTO editBill) {
        AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

        bill.setName(editBill.getName());
        bill.setResponsible(accountMapper.toEntity(editBill.getResponsible()));
        bill.setCompany(editBill.getCompany());
        bill.setCategory(editBill.getCategory());
        bill.setTipPercent(editBill.getTipPercent());
//        bill.setItems();
        editBill.getItems().stream().map(I);
    }

}
