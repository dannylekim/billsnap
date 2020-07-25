package proj.kedabra.billsnap.business.model.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
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

import proj.kedabra.billsnap.business.utils.enums.AccountStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.GenderEnum;

@Data
@Entity
@Table(name = "account", schema = "public")
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType.class
)
public class Account implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "account_id_seq")
    @SequenceGenerator(name = "account_id_seq", sequenceName = "account_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "first_name", length = 30, nullable = false)
    private String firstName;

    @Column(name = "middle_name", length = 20)
    private String middleName;

    @Column(name = "last_name", length = 30, nullable = false)
    private String lastName;

    @Column(name = "gender", columnDefinition = "gender")
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    private GenderEnum gender;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "status", nullable = false, columnDefinition = "status")
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    private AccountStatusEnum status;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @OneToMany(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<AccountGroup> groups = new HashSet<>();

    @OneToMany(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<AccountBill> bills = new HashSet<>();

    @OneToMany(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<AccountItem> items = new HashSet<>();

    @OneToMany(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Notifications> notifications = new HashSet<>();

    public Optional<AccountBill> getAccountBill(final Bill bill) {
        return bills.stream().filter(ab -> ab.getBill().equals(bill)).findFirst();
    }

}
