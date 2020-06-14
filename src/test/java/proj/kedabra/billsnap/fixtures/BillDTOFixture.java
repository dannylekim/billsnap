package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.ArrayList;

import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.dto.TaxDTO;

public final class BillDTOFixture {

    private BillDTOFixture() { }

    public static BillDTO getDefault() {

        final var billDTO = new BillDTO();
        billDTO.setName("Monthly Rent");
        billDTO.setCategory("Rent");
        billDTO.setCompany("Landlord");
        billDTO.setTipAmount(BigDecimal.ZERO);

        final ArrayList<ItemDTO> items = new ArrayList<>();
        final var itemDTO = new ItemDTO();
        itemDTO.setName("Rent");
        itemDTO.setCost(BigDecimal.valueOf(300));
        items.add(itemDTO);
        billDTO.setItems(items);

        final var tax = new TaxDTO();
        tax.setName("Tax 1");
        tax.setPercentage(BigDecimal.TEN);

        final ArrayList<TaxDTO> taxes = new ArrayList<>();
        taxes.add(tax);
        billDTO.setTaxes(taxes);

        billDTO.setAccountsList(new ArrayList<>());

        return billDTO;
    }

}
