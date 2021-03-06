package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.model.entities.Notifications;
import proj.kedabra.billsnap.business.service.NotificationService;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class NotificationServiceImplIT {

    @Autowired
    private NotificationService notificationService;

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
        assertThat(notification.getTimeSent()).isCloseTo(ZonedDateTime.now(ZoneId.systemDefault()), within(500, ChronoUnit.MILLIS));

    }
}
