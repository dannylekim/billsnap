package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public final class LocationResource implements Serializable {

    @Schema(description = "Name of location")
    private String name;

    @Schema(description = "Description of location")
    private String description;

    @Schema(description = "Address of location")
    private String address;

    @Schema(description = "City of location")
    private String city;

    @Schema(description = "State of location")
    private String state;

    @Schema(description = "Country of location")
    private String country;

    @Schema(description = "Postal code of location")
    private String postalCode;
}
