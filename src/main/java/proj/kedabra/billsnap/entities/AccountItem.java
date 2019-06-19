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
@Entity(name = "AccountItem")
@Table(name = "items_vs_accounts")
public class AccountItem {

    @EmbeddedId
    private AccountItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountId")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itemId")
    private Item item;

    @Column(name = "percentage", precision = 4, scale = 4)
    private BigDecimal percentage;
}
