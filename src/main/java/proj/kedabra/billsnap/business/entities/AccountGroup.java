package proj.kedabra.billsnap.business.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.GroupRoleEnum;

@Data
@Entity
@Table(name = "groups_vs_accounts", schema = "public")
public class AccountGroup implements Serializable {

    private static final long serialVersionUID = 1639503885990999000L;

    @EmbeddedId
    private AccountGroupId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountId")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    private Group group;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupRoleEnum groupRole;

}
