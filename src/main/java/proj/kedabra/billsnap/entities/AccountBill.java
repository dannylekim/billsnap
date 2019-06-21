package proj.kedabra.billsnap.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "bills_vs_accounts", schema = "public")
public class AccountBill {

    @EmbeddedId
    private AccountBillId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountId")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("billId")
    private Bill bill;

    @Column(name = "percentage", precision = 4, scale = 4, nullable = false)
    private BigDecimal percentage;
}
