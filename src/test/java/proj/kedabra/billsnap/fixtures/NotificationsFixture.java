package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.business.model.entities.Notifications;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class NotificationsFixture {

    private NotificationsFixture() {
    }

    public static Notifications getDefault() {
        final Notifications notification = new Notifications();
        notification.setId(1425L);
        notification.setAccount(AccountEntityFixture.getDefaultAccount());
        notification.setBill(BillEntityFixture.getMappedBillSplitDTOFixture());
        notification.setTimeSent(ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()));
        return notification;
    }

}
