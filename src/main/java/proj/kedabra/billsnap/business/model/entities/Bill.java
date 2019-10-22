package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.SplitByEnum;

@Data
@Entity
@Table(name = "bill", schema = "public")
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType.class
)
public class Bill implements Serializable {

    private static final long serialVersionUID = 5673217785097106248L;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "bill_id_seq")
    @SequenceGenerator(name = "bill_id_seq", sequenceName = "bill_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", length = 30)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible", referencedColumnName = "id", nullable = false)
    private Account responsible;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator", referencedColumnName = "id", nullable = false)
    private Account creator;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    private BillStatusEnum status;

    @Column(name = "created")
    private ZonedDateTime created = ZonedDateTime.now(ZoneId.systemDefault());

    @Column(name = "updated")
    private ZonedDateTime updated = ZonedDateTime.now(ZoneId.systemDefault());

    @Column(name = "category", length = 20)
    private String category;

    @Column(name = "company", length = 20)
    private String company;

    @Column(name = "occurrence")
    private Integer occurrence;

    @Column(name = "tip_percent", precision = 7, scale = 4)
    private BigDecimal tipPercent;

    @Column(name = "tip_amount", precision = 14, scale = 2)
    private BigDecimal tipAmount;

    @Column(name = "split_by", nullable = false)
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    private SplitByEnum splitBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Item> items = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Tax> taxes = new HashSet<>();

    @OneToMany(mappedBy = "bill", orphanRemoval = true, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<AccountBill> accounts = new HashSet<>();
}
