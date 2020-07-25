package proj.kedabra.billsnap.business.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;

import proj.kedabra.billsnap.business.model.entities.Location;

@Data
public class BaseAccountDTO implements Serializable {

    protected String firstName;

    protected String middleName;

    protected String lastName;

    protected String gender;

    protected String phoneNumber;

    protected LocalDate birthDate;

    protected Location location;

}
