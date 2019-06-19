package proj.kedabra.billsnap.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class AccountGroupId implements Serializable {

    private static final long serialVersionUID = 8264971067739711860L;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "group_id")
    private Long groupId;

}
