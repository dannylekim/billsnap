package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.ArrayList;

import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.ItemDTO;

public class BillDTOFixture {

    private BillDTOFixture() { }

    public static BillDTO getDefault() {

        final var billDTO = new BillDTO();
        billDTO.setName("Monthly Rent");
        billDTO.setCategory("Rent");
        billDTO.setCompany("Landlord");

        final ArrayList<ItemDTO> items = new ArrayList<>();
        final var itemDTO = new ItemDTO();
        itemDTO.setName("Rent");
        itemDTO.setCost(BigDecimal.valueOf(300));
        items.add(itemDTO);
        billDTO.setItems(items);

        return billDTO;
    }

}
