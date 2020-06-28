package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import proj.kedabra.billsnap.presentation.resources.EditBillResource;
import proj.kedabra.billsnap.presentation.resources.ItemResource;

public final class EditBillResourceFixture {

    private EditBillResourceFixture() {}

    public static EditBillResource getDefault() {
        final EditBillResource editBillResource = new EditBillResource();
        editBillResource.setName("editName");
        editBillResource.setResponsible(AccountResourceFixture.getDefault().getEmail());
        editBillResource.setCompany("editCompany");
        editBillResource.setCategory("editCategory");
        editBillResource.setTipPercent(BigDecimal.valueOf(25));

        List<ItemResource> items = new ArrayList<>();
        final var item1 = new ItemResource();
        item1.setId(1013L);
        item1.setCost(BigDecimal.TEN);
        item1.setName("stone");
        final var item2 = new ItemResource();
        item2.setId(null);
        item2.setCost(BigDecimal.valueOf(75));
        item2.setName("rock");
        items.add(item1);
        items.add(item2);
        editBillResource.setItems(items);

        final var taxResource = TaxResourceFixture.getDefault();
        editBillResource.setTaxes(List.of(taxResource));

        return editBillResource;
    }
}
