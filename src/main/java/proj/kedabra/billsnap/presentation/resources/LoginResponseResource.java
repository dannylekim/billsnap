package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LoginResponseResource implements Serializable {

    private static final long serialVersionUID = -7842886525110775521L;

    @ApiModelProperty(name = "Login success message")
    private String message;

    @ApiModelProperty(name = "Login bearer token", position = 1)
    private String token;
}
