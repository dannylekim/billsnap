package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.List;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;

public class AssociateBillDTOFixture {

    private AssociateBillDTOFixture() {}

    public static AssociateBillDTO getDefault() {
        final var dto = new AssociateBillDTO();

        final var itemAssociationDtoOne = ItemAssociationDTOFixture.getDefault();
        itemAssociationDtoOne.setAccountEmail("test@email.com");
        final var itemPercentageDtoOne = ItemPercentageDTOFixture.getDefault();
        itemPercentageDtoOne.setId(1L);
        itemPercentageDtoOne.setPercentage(BigDecimal.valueOf(50));
        itemAssociationDtoOne.setItems(List.of(itemPercentageDtoOne));

        final var itemAssociationDtoTwo = ItemAssociationDTOFixture.getDefault();
        itemAssociationDtoTwo.setAccountEmail("userdetails@service.com");
        final var itemPercentageDtoTwo = ItemPercentageDTOFixture.getDefault();
        itemPercentageDtoTwo.setId(1L);
        itemPercentageDtoTwo.setPercentage(BigDecimal.valueOf(50));
        itemAssociationDtoTwo.setItems(List.of(itemPercentageDtoTwo));

        dto.setId(1002L);
        dto.setItems(List.of(itemAssociationDtoOne, itemAssociationDtoTwo));

        return dto;
    }
}
