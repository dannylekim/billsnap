package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.PaymentStatusEnum;
import proj.kedabra.billsnap.fixtures.PaymentInformationDTOFixture;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class PaymentFacadeImplIT {

    @Autowired
    private PaymentFacadeImpl paymentFacade;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should return an exception if the account does not exist")
    void shouldReturnExceptionIfAccountDoesNotExist() {
        //Given
        final String nonExistentEmail = "fuckingfakemail@castingcouch.com";

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> paymentFacade.getAmountsOwed(nonExistentEmail))
                .withMessage("Account does not exist");
    }

    @Test
    @DisplayName("Should Return a List of 2 Amount Owed")
    void shouldReturnListOf2AmountOwed() {
        //Given
        final String email = "paymentowed@test.com";

        //When
        final List<PaymentOwedDTO> paymentOwedList = paymentFacade.getAmountsOwed(email);

        //Then
        assertThat(paymentOwedList.size()).isEqualTo(2);
        assertThat(paymentOwedList.get(0).getEmail()).isEqualTo("user@user.com");
        assertThat(paymentOwedList.get(0).getAmount().toString()).isEqualTo("133.00");
        assertThat(paymentOwedList.get(1).getEmail()).isEqualTo("userdetails@service.com");
        assertThat(paymentOwedList.get(1).getAmount().toString()).isEqualTo("489.00");
    }

    @Test
    @DisplayName("Should return empty array for payments owed to oneself")
    void shouldReturnEmptyListIfSoleResponsible() {
        //Given
        final String email = "paymentOwedToMe@email.com";

        //When
        final List<PaymentOwedDTO> paymentOwedList = paymentFacade.getAmountsOwed(email);

        //Then
        assertThat(paymentOwedList.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should throw exception if account does not exist")
    void shouldThrowExceptionOnInexistingAccount() {
        //given
        final var email = "fakeaf@email.com";
        final var billId = 1002L;

        //when/then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> paymentFacade.payBill(PaymentInformationDTOFixture.getDefaultWithBillAndEmail(billId, email)));
    }

    @Test
    @DisplayName("Should throw exception if Bill does not exist")
    void shouldThrowExceptionOnInexistingBill() {
        //given
        final var email = "paymentowed@test.com";
        final var billId = 99919294L;

        //when/then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> paymentFacade.payBill(PaymentInformationDTOFixture.getDefaultWithBillAndEmail(billId, email)));
    }

    @Test
    @DisplayName("Should save in database amount paid by everyone but the responsible and new status")
    void shouldSaveInDbAmountPaidAndStatus() {
        //Given
        final var email = "owed@user.com";
        final var billId = 1004L;
        final var paymentInfo = PaymentInformationDTOFixture.getDefaultWithBillAndEmail(billId, email);
        final var amount = new BigDecimal("400");
        paymentInfo.setAmount(amount);

        //When
        final var remainingBalance = paymentFacade.payBill(paymentInfo);

        //Then
        final var account = accountRepository.getAccountByEmail(email);
        final var accountBill = account.getBills().stream().filter(ab -> ab.getBill().getId().equals(billId)).findFirst().orElseThrow();
        assertThat(accountBill.getAmountPaid()).isEqualTo(amount);
        assertThat(accountBill.getPaymentStatus()).isEqualTo(PaymentStatusEnum.PAID);
        final var bill = accountBill.getBill();
        assertThat(bill.getStatus()).isEqualTo(BillStatusEnum.RESOLVED);
        assertThat(remainingBalance).usingComparator(BigDecimal::compareTo).isEqualTo(BigDecimal.ZERO);
    }

}
