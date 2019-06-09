package proj.kedabra.billsnap.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "group", schema = "public")
public class Group implements Serializable {
    private static final long serialVersionUID = -6156582594279723983L;

    @Id
    private Long id;
    private String name;
    private ZonedDateTime created;
    private ZonedDateTime updated;
    private boolean approvalOption;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "groups_vs_accounts", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "bill_id"))
    private List<Bill> bills = new ArrayList<>();

}
