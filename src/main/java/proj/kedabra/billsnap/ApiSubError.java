package proj.kedabra.billsnap;

import org.springframework.validation.FieldError;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ApiSubError {

    @ApiModelProperty(name = "Field that contains the error")
    private String field;

    @ApiModelProperty(name = "The value of the field that has the error", position = 1)
    private Object rejectedValue;

    @ApiModelProperty(name = "The error message that details the invalid field's value", position = 2)
    private String message;

    public ApiSubError(FieldError error) {
        this.field = error.getField();
        this.rejectedValue = error.getRejectedValue();
        this.message = error.getDefaultMessage();
    }


}
