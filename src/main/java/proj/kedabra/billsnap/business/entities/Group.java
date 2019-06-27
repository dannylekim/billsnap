package proj.kedabra.billsnap.business.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "group", schema = "public")
public class Group implements Serializable {

    private static final long serialVersionUID = -6156582594279723983L;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "GROUPS_id_seq")
    @SequenceGenerator(name = "GROUPS_id_seq", sequenceName = "GROUPS_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "created")
    private ZonedDateTime created;

    @Column(name = "updated")
    private ZonedDateTime updated;

    @Column(name = "approval_option", nullable = false)
    private Boolean approvalOption;

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinTable(name = "bills_vs_groups",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "bill_id"))
    private List<Bill> bills = new ArrayList<>();

    @OneToMany(mappedBy = "group", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AccountGroup> accounts = new ArrayList<>();
}
