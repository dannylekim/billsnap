package proj.kedabra.billsnap.business.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.temporal.ChronoUnit;

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
    @DisplayName("Should map BillSplitDTO to PendingRegisteredBillSplitDTO")
    void shouldMapBillSplitDTOtoPendingRegisteredBillSplitDTO() {
        //Given
        final var billSplitDTO = BillSplitDTOFixture.getDefault();
        billSplitDTO.setId(9001L);
        final var name = "billmappertest";
        billSplitDTO.setName(name);

        //When
        final var pendingRegisteredBillSplitDTO = billMapper.toPendingRegisteredBillSplitDTO(billSplitDTO);

        //Then
        assertThat(pendingRegisteredBillSplitDTO.getId()).isEqualTo(billSplitDTO.getId());
        assertThat(pendingRegisteredBillSplitDTO.getName()).isEqualTo(billSplitDTO.getName());
        assertThat(pendingRegisteredBillSplitDTO.getBalance()).isEqualTo(billSplitDTO.getBalance());
        assertThat(pendingRegisteredBillSplitDTO.getCategory()).isEqualTo(billSplitDTO.getCategory());
        assertThat(pendingRegisteredBillSplitDTO.getCompany()).isEqualTo(billSplitDTO.getCompany());
        assertThat(pendingRegisteredBillSplitDTO.getCreator()).isEqualTo(billSplitDTO.getCreator());
        assertThat(pendingRegisteredBillSplitDTO.getItemsPerAccount()).isEqualTo(billSplitDTO.getItemsPerAccount());
        assertThat(pendingRegisteredBillSplitDTO.getResponsible()).isEqualTo(billSplitDTO.getResponsible());
        assertThat(pendingRegisteredBillSplitDTO.getSplitBy()).isEqualTo(billSplitDTO.getSplitBy());
        assertThat(pendingRegisteredBillSplitDTO.getStatus()).isEqualTo(billSplitDTO.getStatus());
        assertThat(pendingRegisteredBillSplitDTO.getTotalTip()).isEqualTo(billSplitDTO.getTotalTip());
        assertThat(pendingRegisteredBillSplitDTO.getCreated()).isCloseTo(billSplitDTO.getCreated(), within(500, ChronoUnit.MILLIS));
        assertThat(pendingRegisteredBillSplitDTO.getPendingAccounts()).isNull();

    }

    @Test
    @DisplayName("Should map editbillDTO to bill")
    void shouldMapEditBillDtoToBill() {
        //Given
        final var editbill = EditBillDTOFixture.getDefault();
        final var bill = BillEntityFixture.getDefault();
        final var accountEntity = new Account();
        accountEntity.setEmail(editbill.getResponsible().getEmail());
        accountEntity.setId(editbill.getResponsible().getId());
        accountEntity.setFirstName(editbill.getResponsible().getFirstName());
        accountEntity.setLastName(editbill.getResponsible().getLastName());

        //When
        billMapper.updatebill(bill, editbill);

        //Then
        assertThat(bill.getName()).isEqualTo(editbill.getName());
        assertThat(bill.getResponsible().getEmail()).isEqualTo(editbill.getResponsible().getEmail());
        assertThat(bill.getResponsible().getId()).isEqualTo(editbill.getResponsible().getId());
        assertThat(bill.getResponsible().getFirstName()).isEqualTo(editbill.getResponsible().getFirstName());
        assertThat(bill.getResponsible().getLastName()).isEqualTo(editbill.getResponsible().getLastName());
        assertThat(bill.getResponsible().getMiddleName()).isEqualTo(editbill.getResponsible().getMiddleName());
        assertThat(bill.getCompany()).isEqualTo(editbill.getCompany());
        assertThat(bill.getCategory()).isEqualTo(editbill.getCategory());
    }
}