package proj.kedabra.billsnap.business.exception;

import java.util.List;

import org.springframework.validation.ObjectError;

import lombok.Getter;

@Getter
public class FieldValidationException extends BillSnapException {

    private final List<ObjectError> errors;

    public FieldValidationException(List<ObjectError> errors) {
        super("Invalid Inputs. Please fix the following errors");
        this.errors = errors;
    }
}
