package proj.kedabra.billsnap.exception;

import java.util.List;

import org.springframework.validation.ObjectError;

import lombok.Getter;

@Getter
public class FieldValidationException extends BillSnapException {

    private static final long serialVersionUID = 4200035927552159732L;

    private List<ObjectError> errors;

    public FieldValidationException(String message, Throwable ex, List<ObjectError> errors) {
        super(message, ex);
        this.errors = errors;
    }
}
