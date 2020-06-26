package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ItemAssociationResource implements Serializable {

    @NotBlank
    @Email(message = "{email.emailFormat}")
    @Size(max = 50)
    @Schema(description = "Account to associate users with")
    private String email;

    @NotNull
    @Schema(description = "List of items that the user will be associated with")
    private List<@Valid ItemPercentageResource> items;
}
