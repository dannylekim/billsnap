package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class AccountBillId implements Serializable {

    private static final long serialVersionUID = -7567078408665995383L;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "bill_id", nullable = false)
    private Long billId;
}
