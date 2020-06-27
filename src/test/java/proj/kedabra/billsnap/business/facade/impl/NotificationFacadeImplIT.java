package proj.kedabra.billsnap.business.facade.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import proj.kedabra.billsnap.business.dto.AnswerNotificationDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.facade.NotificationFacade;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Notifications;
import proj.kedabra.billsnap.business.repository.NotificationsRepository;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AnswerNotificationDTOFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
import proj.kedabra.billsnap.utils.SpringProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
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
        final long invitationId = 101L;
        final boolean answer = true;
        final String email = "user@inbill.com";
        final AnswerNotificationDTO answerNotificationDTO = AnswerNotificationDTOFixture.getDefault();
        answerNotificationDTO.setInvitationId(invitationId);
        answerNotificationDTO.setAnswer(answer);
        answerNotificationDTO.setEmail(email);

        //When
        final BillSplitDTO billSplitDTO = notificationFacade.answerInvitation(answerNotificationDTO);

        //Then
        final Notifications notification = notificationsRepository.findById(invitationId).get();
        final AccountBill accountBill = notification.getBill().getAccountBill(notification.getAccount()).get();
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.ACCEPTED);
        assertThat(billSplitDTO).isInstanceOf(BillSplitDTO.class);
    }

    @Test
    @DisplayName("Should set AccountBill status to DECLINED if answer is Decline")
    void shouldSetAccountBillStatusToDeclinedIfAnswerIsDecline() {
        //Given
        final long invitationId = 101L;
        final boolean answer = false;
        final String email = "user@inbill.com";
        final AnswerNotificationDTO answerNotificationDTO = AnswerNotificationDTOFixture.getDefault();
        answerNotificationDTO.setInvitationId(invitationId);
        answerNotificationDTO.setAnswer(answer);
        answerNotificationDTO.setEmail(email);

        //When
        final BillSplitDTO billSplitDTO = notificationFacade.answerInvitation(answerNotificationDTO);

        //Then
        final Notifications notification = notificationsRepository.findById(invitationId).get();
        final AccountBill accountBill = notification.getBill().getAccountBill(notification.getAccount()).get();
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.DECLINED);
        assertThat(billSplitDTO).isInstanceOf(BillSplitDTO.class);
    }

    @Test
    @DisplayName("Should throw exception if invitation id is not valid")
    void shouldThrowExceptionIfInvitationIdDoesNotExist() {
        //Given
        final long nonExistentId = 99929141L;
        final boolean answer = false;
        final String email = "user@inbill.com";
        final AnswerNotificationDTO answerNotificationDTO = AnswerNotificationDTOFixture.getDefault();
        answerNotificationDTO.setInvitationId(nonExistentId);
        answerNotificationDTO.setAnswer(answer);
        answerNotificationDTO.setEmail(email);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> notificationFacade.answerInvitation(answerNotificationDTO))
                .withMessage(ErrorMessageEnum.NOTIFICATION_ID_DOES_NOT_EXIST.getMessage(Long.toString(nonExistentId)));

    }

    @Test
    @DisplayName("Should throw exception if user not associated to notification")
    void shouldThrowExceptionIfUserNotAssociatedToNotification() {
        //Given
        final long invitationId = 102L;
        final String email = "user@inbill.com";
        final AnswerNotificationDTO answerNotificationDTO = AnswerNotificationDTOFixture.getDefault();
        answerNotificationDTO.setInvitationId(invitationId);
        answerNotificationDTO.setEmail(email);

        //When/Then
        assertThatExceptionOfType(AccessForbiddenException.class)
                .isThrownBy(() -> notificationFacade.answerInvitation(answerNotificationDTO))
                .withMessage(ErrorMessageEnum.ACCOUNT_NOT_ASSOCIATED_TO_NOTIFICATION.getMessage());

    }

}