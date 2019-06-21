package proj.kedabra.billsnap.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

import lombok.Data;

import proj.kedabra.billsnap.utils.enums.AccountStatusEnum;
import proj.kedabra.billsnap.utils.enums.GenderEnum;

@Data
@Entity
@Table(name = "account", schema = "public")
public class Account implements Serializable {

    private static final long serialVersionUID = 8311925371311988518L;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "USER_id_seq")
    @SequenceGenerator(name = "USER_id_seq", sequenceName = "USER_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "password", length = 20)
    private String password;

    @Column(name = "first_name", length = 30, nullable = false)
    private String firstName;

    @Column(name = "middle_name", length = 20)
    private String middleName;

    @Column(name = "last_name", length = 30, nullable = false)
    private String lastName;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatusEnum status;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @OneToMany(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AccountGroup> groups = new ArrayList<>();

    @OneToMany(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AccountBill> bills = new ArrayList<>();

    @OneToMany(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AccountItem> items = new ArrayList<>();
}
