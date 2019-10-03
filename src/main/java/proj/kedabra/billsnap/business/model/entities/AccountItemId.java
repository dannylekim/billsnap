package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class AccountItemId implements Serializable {

    private static final long serialVersionUID = 1254886237437391027L;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;
}
