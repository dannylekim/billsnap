package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import proj.kedabra.billsnap.business.model.entities.Notifications;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;

class NotificationServiceImplTest {

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        notificationService = new NotificationServiceImpl();
    }

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

}