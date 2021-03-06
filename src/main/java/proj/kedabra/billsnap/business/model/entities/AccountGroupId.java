package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class AccountGroupId implements Serializable {

    private static final long serialVersionUID = 8264971067739711860L;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

}
