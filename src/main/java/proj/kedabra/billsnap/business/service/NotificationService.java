package proj.kedabra.billsnap.business.service;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Notifications;

public interface NotificationService {

    Notifications createNotification(Bill bill, Account account);

}
