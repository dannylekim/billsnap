package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.time.LocalDate;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import proj.kedabra.billsnap.business.entities.Location;

@Data
public class AccountResource implements Serializable {

    private static final long serialVersionUID = 3234969136011347459L;

    @ApiModelProperty(name = "Unique ID of the user")
    private Long id;

    @ApiModelProperty(name = "Email of the user", position = 1)
    private String email;

    @ApiModelProperty(name = "First name of the user", position = 2)
    private String firstName;

    @ApiModelProperty(name = "Middle name of the user", position = 3)
    private String middleName;

    @ApiModelProperty(name = "Middle name of the user", position = 4)
    private String lastName;

    @ApiModelProperty(name = "Gender of the user", position = 5)
    private String gender;

    @ApiModelProperty(name = "Phone number of the user", position = 6)
    private String phoneNumber;

    @ApiModelProperty(name = "Birth date of the user", position = 7)
    private LocalDate birthDate;

    @ApiModelProperty(name = "Location of the User", position = 8)
    private Location location;

}
