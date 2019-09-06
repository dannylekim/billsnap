package proj.kedabra.billsnap.fixtures;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;

public final class AssociateBillDTOFixture {

    private AssociateBillDTOFixture() {}

    public static AssociateBillDTO getDefault() {
        final var associateBillDTO = new AssociateBillDTO();
        associateBillDTO.setId(1250L);
        associateBillDTO.setItems(Stream.of((ItemAssociationDTOFixture.getDefault())).collect(Collectors.toList()));
        return associateBillDTO;
    }

}
