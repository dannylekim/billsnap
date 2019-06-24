package proj.kedabra.billsnap.business.exception;

import java.util.List;

import org.springframework.validation.ObjectError;

import lombok.Getter;

@Getter
public class FieldValidationException extends BillSnapException {

    private static final long serialVersionUID = 4200035927552159732L;

    private final List<ObjectError> errors;

    public FieldValidationException(List<ObjectError> errors) {
        super("Invalid Inputs. Please fix the following errors");
        this.errors = errors;
    }
}
