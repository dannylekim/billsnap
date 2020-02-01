package proj.kedabra.billsnap.business.repository;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Notifications;

@Repository
public interface NotificationsRepository extends CrudRepository<Notifications, Long> {

    Optional<Notifications> getNotificationsByBillAndAccount(Bill bill, Account account);

}
