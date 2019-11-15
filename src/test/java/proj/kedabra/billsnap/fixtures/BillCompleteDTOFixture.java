package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.ArrayList;

import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

public class BillCompleteDTOFixture {

    private BillCompleteDTOFixture() { }

    public static BillCompleteDTO getDefault() {

        final var billCompleteDTO = new BillCompleteDTO();
        billCompleteDTO.setName("Monthly Rent");
        billCompleteDTO.setCategory("Rent");
        billCompleteDTO.setCompany("Landlord");
        billCompleteDTO.setTipAmount(BigDecimal.ZERO);
        billCompleteDTO.setCreator(AccountDTOFixture.getCreationDTO());
        billCompleteDTO.setResponsible(AccountDTOFixture.getCreationDTO());
        billCompleteDTO.setStatus(BillStatusEnum.OPEN);

        final ArrayList<ItemDTO> items = new ArrayList<>();
        final var itemDTO = new ItemDTO();
        itemDTO.setName("Rent");
        itemDTO.setCost(BigDecimal.valueOf(300));
        items.add(itemDTO);
        billCompleteDTO.setItems(items);

        billCompleteDTO.setAccountsList(new ArrayList<>());
        return billCompleteDTO;
    }
}
