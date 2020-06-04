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
        editBill.setResponsible(AccountDTOFixture.getCreationDTO());
        editBill.setCompany("editCompany");
        editBill.setCategory("editCategory");
        editBill.setTipAmount(BigDecimal.valueOf(15));

        List<ItemDTO> list = new ArrayList<>();
        list.add(ItemDTOFixture.getItemCustom(10l));
        list.add(ItemDTOFixture.getItemCustom(15l));
        list.add(ItemDTOFixture.getItemCustom(20l));
        editBill.setItems(list);

        return editBill;
    }

}
