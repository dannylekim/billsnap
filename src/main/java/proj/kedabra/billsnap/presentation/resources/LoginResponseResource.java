package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginResponseResource implements Serializable {

    @Schema(description = "Login success message")
    private String message;

    @Schema(description = "Profile information")
    private AccountResource profile;

    @Schema(description = "Login bearer token")
    private String token;
}
