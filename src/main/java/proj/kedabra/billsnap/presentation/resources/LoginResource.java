package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LoginResource implements Serializable {

    private static final long serialVersionUID = 1343685026090561386L;

    @NotBlank
    @Email(message = "Must be in an email format. ex: test@email.com.")
    @Size(max = 50)
    @ApiModelProperty(name = "Login username")
    private String email;

    @NotBlank
    @Size(max = 20)
    @ApiModelProperty(name = "Login password", position = 1)
    private String password;
}
