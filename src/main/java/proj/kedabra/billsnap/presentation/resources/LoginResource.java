package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginResource implements Serializable {

    @NotBlank
    @Email(message = "Must be in an email format. ex: test@email.com.")
    @Size(max = 50)
    @Schema(description = "Login username")
    private String email;

    @NotBlank
    @Size(max = 20)
    @Schema(description = "Login password")
    private String password;
}
