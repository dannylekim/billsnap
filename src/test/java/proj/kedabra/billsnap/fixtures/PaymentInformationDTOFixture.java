package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.business.dto.PaymentInformationDTO;

public final class PaymentInformationDTOFixture {

    private PaymentInformationDTOFixture() {}

    public static PaymentInformationDTO getDefaultWithBillAndEmail(final Long billId, final String email) {
        final var paymentInformationDTO = new PaymentInformationDTO();
        paymentInformationDTO.setAmount(BigDecimal.TEN);
        paymentInformationDTO.setEmail(email);
        paymentInformationDTO.setBillId(billId);

        return paymentInformationDTO;
    }
}
