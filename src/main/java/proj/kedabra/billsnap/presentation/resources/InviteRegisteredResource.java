package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class InviteRegisteredResource implements Serializable {

    @NotEmpty
    @Schema(description = "List of registered accounts to invite to bill")
    private List<
            @NotBlank
            @Email(message = "{email.emailFormat}")
            @Size(max = 50) String> accounts;

}
