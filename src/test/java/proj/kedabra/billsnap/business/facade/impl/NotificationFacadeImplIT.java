package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AnswerNotificationDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.facade.NotificationFacade;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Notifications;
import proj.kedabra.billsnap.business.repository.NotificationsRepository;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AnswerNotificationDTOFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@Transactional
class NotificationFacadeImplIT {

    @Autowired
    NotificationFacade notificationFacade;

    @Autowired
    NotificationsRepository notificationsRepository;

    @Test
    @DisplayName("Should set AccountBill status to ACCEPTED if answer is Accept")
    void shouldSetAccountBillStatusToAcceptedIfAnswerIsAccept() {
        //Given
        final long billId = 1220L;
        final long invitationId = 101L;
        final boolean answer = true;
        final String email = "user@inbill.com";
        final AnswerNotificationDTO answerNotificationDTO = AnswerNotificationDTOFixture.getDefault();
        answerNotificationDTO.setBillId(billId);
        answerNotificationDTO.setAnswer(answer);
        answerNotificationDTO.setEmail(email);

        //When
        final BillSplitDTO billSplitDTO = notificationFacade.answerInvitation(answerNotificationDTO);

        //Then
        final Notifications notification = notificationsRepository.findById(invitationId).orElseThrow();
        final AccountBill accountBill = notification.getBill().getAccountBill(notification.getAccount()).orElseThrow();
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.ACCEPTED);
        assertThat(billSplitDTO).isInstanceOf(BillSplitDTO.class);
    }

    @Test
    @DisplayName("Should set AccountBill status to DECLINED if answer is Decline")
    void shouldSetAccountBillStatusToDeclinedIfAnswerIsDecline() {
        //Given
        final long billId = 1220L;
        final long invitationId = 101L;
        final boolean answer = false;
        final String email = "user@inbill.com";
        final AnswerNotificationDTO answerNotificationDTO = AnswerNotificationDTOFixture.getDefault();
        answerNotificationDTO.setBillId(billId);
        answerNotificationDTO.setAnswer(answer);
        answerNotificationDTO.setEmail(email);

        //When
        final BillSplitDTO billSplitDTO = notificationFacade.answerInvitation(answerNotificationDTO);

        //Then
        final Notifications notification = notificationsRepository.findById(invitationId).orElseThrow();
        final AccountBill accountBill = notification.getBill().getAccountBill(notification.getAccount()).orElseThrow();
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.DECLINED);
        assertThat(billSplitDTO).isInstanceOf(BillSplitDTO.class);
    }

    @Test
    @DisplayName("Should throw exception if bill id is not valid")
    void shouldThrowExceptionIfBillIdDoesNotExist() {
        //Given
        final long nonExistentId = 99929141L;
        final boolean answer = false;
        final String email = "user@inbill.com";
        final AnswerNotificationDTO answerNotificationDTO = AnswerNotificationDTOFixture.getDefault();
        answerNotificationDTO.setBillId(nonExistentId);
        answerNotificationDTO.setAnswer(answer);
        answerNotificationDTO.setEmail(email);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> notificationFacade.answerInvitation(answerNotificationDTO))
                .withMessage(ErrorMessageEnum.BILL_ID_DOES_NOT_EXIST.getMessage(Long.toString(nonExistentId)));

    }

    @Test
    @DisplayName("Should throw exception if user not associated to any notification")
    void shouldThrowExceptionIfUserNotAssociatedToNotification() {
        //Given
        final long billId = 1220;
        final String email = "userNot@inbill.com";
        final AnswerNotificationDTO answerNotificationDTO = AnswerNotificationDTOFixture.getDefault();
        answerNotificationDTO.setBillId(billId);
        answerNotificationDTO.setEmail(email);

        //When/Then
        assertThatExceptionOfType(AccessForbiddenException.class)
                .isThrownBy(() -> notificationFacade.answerInvitation(answerNotificationDTO))
                .withMessage(ErrorMessageEnum.ACCOUNT_NOT_ASSOCIATED_TO_NOTIFICATION.getMessage());

    }

    @Test
    @DisplayName("Should throw exception if bill is not Open status")
    void shouldThrowExceptionIfBillNotOpenStatus() {
        //Given
        final long billId = 1101;
        final String email = "user@inbill.com";
        final AnswerNotificationDTO answerNotificationDTO = AnswerNotificationDTOFixture.getDefault();
        answerNotificationDTO.setBillId(billId);
        answerNotificationDTO.setEmail(email);

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class)
                .isThrownBy(() -> notificationFacade.answerInvitation(answerNotificationDTO))
                .withMessage(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));

    }

    @Test
    @DisplayName("Should throw exception if invitation status is not PENDING")
    void shouldThrowExceptionIfInvitationStatusIsNotPending() {
        //Given
        final long billId = 1221;
        final String email = "user@inbill.com";
        final AnswerNotificationDTO answerNotificationDTO = AnswerNotificationDTOFixture.getDefault();
        answerNotificationDTO.setBillId(billId);
        answerNotificationDTO.setEmail(email);

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class)
                .isThrownBy(() -> notificationFacade.answerInvitation(answerNotificationDTO))
                .withMessage(ErrorMessageEnum.WRONG_INVITATION_STATUS.getMessage(InvitationStatusEnum.PENDING.toString()));
    }

}