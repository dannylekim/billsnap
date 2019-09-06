package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "notifications", schema = "public")
public class Notification implements Serializable {

    private static final long serialVersionUID = 5729865586128253732L;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "notification_id_seq")
    @SequenceGenerator(name = "notification_id_seq", sequenceName = "notification_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bill_id")
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("account_id")
    private Account account;

    @Column(name = "time_sent")
    private ZonedDateTime timeSent = ZonedDateTime.now(ZoneId.systemDefault());

}
