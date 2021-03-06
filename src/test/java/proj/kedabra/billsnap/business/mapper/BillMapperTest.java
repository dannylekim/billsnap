package proj.kedabra.billsnap.business.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.BillSplitDTOFixture;
import proj.kedabra.billsnap.fixtures.EditBillDTOFixture;

@SpringBootTest(classes = {AccountMapperImpl.class, ItemMapperImpl.class, BillMapperImpl.class})
class BillMapperTest {

    @Autowired
    private BillMapper billMapper;

    @Test
    @DisplayName("Should map editbillDTO to bill")
    void shouldMapEditBillDtoToBill() {
        //Given
        final var editbill = EditBillDTOFixture.getDefault();
        final var bill = BillEntityFixture.getDefault();
        final var accountEntity = new Account();
        accountEntity.setEmail(editbill.getResponsible());

        //When
        billMapper.updatebill(bill, editbill);

        //Then
        assertThat(bill.getName()).isEqualTo(editbill.getName());
        assertThat(bill.getCompany()).isEqualTo(editbill.getCompany());
        assertThat(bill.getCategory()).isEqualTo(editbill.getCategory());
    }

    @Test
    @DisplayName("Should return amount remaining for the account specified")
    void shouldReturnAmountOwedForAccount() {
        //Given
        final var billSplitDTO = BillSplitDTOFixture.getDefault();
        final var itemAssociationSplitDTO = billSplitDTO.getInformationPerAccount().get(0);
        itemAssociationSplitDTO.setAmountRemaining(BigDecimal.TEN);

        //When
        final var shortBillResource = billMapper.toShortBillResource(billSplitDTO, itemAssociationSplitDTO.getAccount().getEmail());

        //Then
        assertThat(shortBillResource.getAmountOwed()).isEqualByComparingTo(BigDecimal.TEN);

    }
}