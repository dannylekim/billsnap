package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.time.LocalDate;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import proj.kedabra.billsnap.business.model.entities.Location;

@Data
public class BaseAccountResource implements Serializable {

    @NotBlank
    @Schema(description = "First name of the user")
    protected String firstName;

    @Schema(description = "Middle name of the user")
    protected String middleName;

    @NotBlank
    @Schema(description = "Middle name of the user")
    protected String lastName;

    @Schema(description = "Gender of the user")
    protected String gender;

    @Schema(description = "Phone number of the user")
    protected String phoneNumber;

    @Schema(description = "Birth date of the user")
    protected LocalDate birthDate;

    @Schema(description = "Location of the User")
    protected Location location;
}
