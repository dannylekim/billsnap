package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import proj.kedabra.billsnap.business.model.entities.Location;

@Data
public class AccountResource implements Serializable {

    @Schema(description = "Unique ID of the user")
    private Long id;

    @Schema(description = "Email of the user")
    private String email;

    @Schema(description = "First name of the user")
    private String firstName;

    @Schema(description = "Middle name of the user")
    private String middleName;

    @Schema(description = "Middle name of the user")
    private String lastName;

    @Schema(description = "Gender of the user")
    private String gender;

    @Schema(description = "Phone number of the user")
    private String phoneNumber;

    @Schema(description = "Birth date of the user")
    private LocalDate birthDate;

    @Schema(description = "Location of the User")
    private Location location;

}
