package proj.kedabra.billsnap.business.service.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Notifications;
import proj.kedabra.billsnap.business.repository.NotificationsRepository;
import proj.kedabra.billsnap.business.service.NotificationService;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationsRepository notificationsRepository;

    @Autowired
    public NotificationServiceImpl(final NotificationsRepository notificationsRepository) {
        this.notificationsRepository = notificationsRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notifications createNotification(final Bill bill, final Account account) {
        final var notification = new Notifications();
        notification.setAccount(account);
        notification.setBill(bill);
        notification.setTimeSent(ZonedDateTime.now(ZoneId.systemDefault()));
        bill.getNotifications().add(notification);
        account.getNotifications().add(notification);
        return notification;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill answerInvitation(Long invitationId, boolean answer) {
        final Notifications notification = getNotification(invitationId);
        final Bill bill = notification.getBill();

        final Optional<AccountBill> accountBill = bill.getAccountBill(notification.getAccount());
        accountBill.ifPresent(accBill -> {
            verifyAccountBillInvitationStatus(accBill, InvitationStatusEnum.PENDING);
            if (answer) {
                accBill.setStatus(InvitationStatusEnum.ACCEPTED);
            } else {
                accBill.setStatus(InvitationStatusEnum.DECLINED);
            }
        });

        return bill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Notifications getNotification(Long notificationId) {
        return notificationsRepository.findById(notificationId).orElseThrow(() ->
                new ResourceNotFoundException(ErrorMessageEnum.NOTIFICATION_ID_DOES_NOT_EXIST.getMessage(notificationId.toString())));

    }

    private void verifyAccountBillInvitationStatus(final AccountBill accountBill, final InvitationStatusEnum invitationStatus) {
        if (accountBill.getStatus() != invitationStatus) {
            throw new FunctionalWorkflowException(ErrorMessageEnum.WRONG_INVITATION_STATUS.getMessage(invitationStatus.toString()));
        }
    }

}
