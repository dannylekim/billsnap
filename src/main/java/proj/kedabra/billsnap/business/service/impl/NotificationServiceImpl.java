package proj.kedabra.billsnap.business.service.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Notifications;
import proj.kedabra.billsnap.business.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public Notifications createNotification(final Bill bill, final Account account) {
        final var notification = new Notifications();
        notification.setAccount(account);
        notification.setBill(bill);
        notification.setTimeSent(ZonedDateTime.now(ZoneId.systemDefault()));
        bill.getNotifications().add(notification);
        account.getNotifications().add(notification);
        return notification;
    }
}
