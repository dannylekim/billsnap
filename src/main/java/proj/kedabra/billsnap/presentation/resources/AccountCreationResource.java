package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AccountCreationResource implements Serializable {

    private static final long serialVersionUID = -5979473809095168700L;

    @NotBlank
    @Email(message = "{email.emailFormat}")
    @Size(max = 50)
    @ApiModelProperty(name = "Desired email of the user. Must not already exist, and must be email format and max size of 50. Required")
    private String email;

    @Size(max = 20, min = 8)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[().,/?|'\"`~!@#$%^&+={};:<>*\\[\\]\\\\]).*$", message = "Password must contain an upper and lower case, a number, and a symbol.")
    @ApiModelProperty(name = "Desired password of the user. Min size of 8. Max size of 20. Must contain a number, an upper and lower case, as well as a symbol. Required", position = 1)
    private String password;

    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(name = "First name of the user. Max size of 30. Required", position = 2)
    private String firstName;

    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(name = "Last name of the user. Max size of 30. Required", position = 3)
    private String lastName;
}
