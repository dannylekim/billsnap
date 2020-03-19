package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LoginResponseResource implements Serializable {

    private static final long serialVersionUID = -7842886525110775521L;

    @ApiModelProperty(name = "Login success message")
    private String message;

    @ApiModelProperty(name = "Account firstname", position = 1)
    private String firstName;

    @ApiModelProperty(name = "Account lastname", position = 2)
    private String lastName;

    @ApiModelProperty(name = "Login bearer token", position = 3)
    private String token;
}
