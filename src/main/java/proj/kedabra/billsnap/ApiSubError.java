package proj.kedabra.billsnap;

import org.springframework.validation.FieldError;

import lombok.Data;

@Data
public class ApiSubError {

    private Object[] arguments;

    private String field;

    private String objectName;

    private Object rejectedValue;

    private String message;

    private String errorCode;

    public ApiSubError(FieldError error) {
        this.arguments = error.getArguments();
        this.field = error.getField();
        this.objectName = error.getObjectName();
        this.rejectedValue = error.getRejectedValue();
        this.message = error.getDefaultMessage();
        this.errorCode = error.getCode();
    }


}
