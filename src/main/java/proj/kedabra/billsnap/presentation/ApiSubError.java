package proj.kedabra.billsnap.presentation;

import org.springframework.validation.FieldError;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiSubError {

    @Schema(description = "Field that contains the error")
    private String field;

    @Schema(description = "The value of the field that has the error")
    private Object rejectedValue;

    @Schema(description = "The error message that details the invalid field's value")
    private String message;

    /**
     * Do not use. Solely for testing/mapping purposes
     */
    public ApiSubError(){
    }

    public ApiSubError(FieldError error) {
        this.field = error.getField();
        this.rejectedValue = error.getRejectedValue();
        this.message = error.getDefaultMessage();
    }


}
