package proj.kedabra.billsnap.entities;

import lombok.Data;
import proj.kedabra.billsnap.utils.AccountStatusEnum;
import proj.kedabra.billsnap.utils.GenderEnum;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "account", schema = "public")
public class Account implements Serializable {
    private static final long serialVersionUID = 8311925371311988518L;

    @Id
    private Long id;
    private String email;
    private String password;
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    @Enumerated(EnumType.STRING)
    private GenderEnum gender;
    private String phoneNumber;
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private AccountStatusEnum status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Location location;

}
