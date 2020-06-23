package proj.kedabra.billsnap.fixtures;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import proj.kedabra.billsnap.business.dto.EditBillDTO;
import proj.kedabra.billsnap.business.dto.ItemDTO;

public class EditBillDTOFixture {

    private EditBillDTOFixture() {}

    public static EditBillDTO getDefault() {
        EditBillDTO editBill = new EditBillDTO();
        editBill.setName("editName");
        editBill.setResponsible(AccountDTOFixture.getCreationDTO().getEmail());
        editBill.setCompany("editCompany");
        editBill.setCategory("editCategory");
        editBill.setTipPercent(BigDecimal.valueOf(25));

        List<ItemDTO> list = new ArrayList<>();
        final var item1 = new ItemDTO();
        item1.setId(1000L);
        item1.setCost(BigDecimal.TEN);
        item1.setName("Edit Item");
        final var item2 = ItemDTOFixture.getItemCustom(null);
        list.add(item1);
        list.add(item2);
        editBill.setItems(list);

        return editBill;
    }

}
