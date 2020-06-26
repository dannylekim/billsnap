package proj.kedabra.billsnap.business.repository;

import org.springframework.data.repository.CrudRepository;
import proj.kedabra.billsnap.business.model.entities.Notifications;

public interface NotificationsRepository extends CrudRepository<Notifications, Long> {

}
