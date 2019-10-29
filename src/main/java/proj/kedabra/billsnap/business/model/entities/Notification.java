package proj.kedabra.billsnap.business.model.entities;

import lombok.Data;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "bill", schema = "public")
public class Notification {

    private static final long serialVersionUID = 5729865586128253732L;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "notification_id_seq")
    @SequenceGenerator(name = "notification_id_seq", sequenceName = "notification_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "time_sent")
    private ZonedDateTime timeSent = ZonedDateTime.now(ZoneId.systemDefault());

}
