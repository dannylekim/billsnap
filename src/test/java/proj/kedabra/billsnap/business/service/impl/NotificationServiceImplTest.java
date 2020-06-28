package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Notifications;
import proj.kedabra.billsnap.business.repository.NotificationsRepository;
import proj.kedabra.billsnap.business.service.BillService;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.NotificationsFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;


@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    NotificationsRepository notificationsRepository;

    @Mock
    BillService billService;

    @Test
    @DisplayName("Should return notification")
    void shouldReturnNotification() {
        //Given
        final var account = AccountEntityFixture.getDefaultAccount();
        final var bill = BillEntityFixture.getDefault();
        final var originalAccountNotificationsSize = account.getNotifications().size();
        final var originalBillNotificationsSize = bill.getNotifications().size();

        //When
        final Notifications notification = notificationService.createNotification(bill, account);

        //Then
        assertThat(account.getNotifications().size()).isEqualTo(originalAccountNotificationsSize + 1);
        assertThat(bill.getNotifications().size()).isEqualTo(originalBillNotificationsSize + 1);
        assertThat(notification.getAccount()).isEqualTo(account);
        assertThat(notification.getBill()).isEqualTo(bill);
        assertThat(notification.getTimeSent()).isCloseTo(ZonedDateTime.now(ZoneId.systemDefault()), within(200, ChronoUnit.MILLIS));

    }

    @Test
    @DisplayName("Should set AccountBill status to ACCEPTED if answer is Accept")
    void shouldSetAccountBillStatusToAcceptedIfAnswerIsAccept() {
        //Given
        final long invitationId = 1234L;
        final boolean answer = true;
        final Notifications notification = NotificationsFixture.getDefault();
        final AccountBill accountBill = notification.getBill().getAccountBill(notification.getAccount()).get();
        accountBill.setStatus(InvitationStatusEnum.PENDING);

        given(notificationsRepository.findById(anyLong())).willReturn(Optional.of(notification));

        //When
        notificationService.answerInvitation(invitationId, answer);

        //Then
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.ACCEPTED);

    }

    @Test
    @DisplayName("Should set AccountBill status to DECLINED if answer is Decline")
    void shouldSetAccountBillStatusToDeclinedIfAnswerIsDecline() {
        //Given
        final long invitationId = 1234L;
        final boolean answer = false;
        final Notifications notification = NotificationsFixture.getDefault();
        final AccountBill accountBill = notification.getBill().getAccountBill(notification.getAccount()).get();
        accountBill.setStatus(InvitationStatusEnum.PENDING);

        given(notificationsRepository.findById(anyLong())).willReturn(Optional.of(notification));

        //When
        notificationService.answerInvitation(invitationId, answer);

        //Then
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.DECLINED);

    }

    @Test
    @DisplayName("Should throw exception if invitation id is not valid")
    void shouldThrowExceptionIfInvitationIdDoesNotExist() {
        //Given
        final long nonExistentId = 99929141L;
        final boolean answer = false;
        given(notificationsRepository.findById(anyLong())).willReturn(Optional.empty());

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> notificationService.answerInvitation(nonExistentId, answer))
                .withMessage(ErrorMessageEnum.NOTIFICATION_ID_DOES_NOT_EXIST.getMessage(Long.toString(nonExistentId)));

    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatusEnum.class, names = {"DECLINED", "ACCEPTED"})
    @DisplayName("Should throw exception if invitation status is not PENDING")
    void shouldThrowExceptionIfInvitationStatusIsNotPending(final InvitationStatusEnum invitationStatus) {
        //Given
        final long invitationId = 1234L;
        final boolean answer = false;
        final Notifications notification = NotificationsFixture.getDefault();
        final AccountBill accountBill = notification.getBill().getAccountBill(notification.getAccount()).get();
        accountBill.setStatus(invitationStatus);

        given(notificationsRepository.findById(anyLong())).willReturn(Optional.of(notification));

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class)
                .isThrownBy(() -> notificationService.answerInvitation(invitationId, answer))
                .withMessage(ErrorMessageEnum.WRONG_INVITATION_STATUS.getMessage(InvitationStatusEnum.PENDING.toString()));

    }

}