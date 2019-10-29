package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;

@Data
@Entity
@Table(name = "bills_vs_accounts", schema = "public")
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType.class
)
public class AccountBill implements Serializable {

    private static final long serialVersionUID = 7692602917199916186L;

    @EmbeddedId
    private AccountBillId id = new AccountBillId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountId")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("billId")
    private Bill bill;

    @Column(name = "percentage", precision = 7, scale = 4, nullable = false)
    private BigDecimal percentage;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    private InvitationStatusEnum status;
}
