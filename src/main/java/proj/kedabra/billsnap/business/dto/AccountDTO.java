package proj.kedabra.billsnap.business.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;

import proj.kedabra.billsnap.model.entities.Location;

@Data
public class AccountDTO implements Serializable {

    private static final long serialVersionUID = -8319328053666986576L;

    private Long id;

    private String email;

    private String password;

    private String firstName;

    private String middleName;

    private String lastName;

    private String gender;

    private String phoneNumber;

    private LocalDate birthDate;

    private Location location;

}
