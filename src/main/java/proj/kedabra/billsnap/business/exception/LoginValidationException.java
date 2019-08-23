package proj.kedabra.billsnap.business.exception;

import java.util.List;

import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;

public class LoginValidationException extends AuthenticationException {

    private static final long serialVersionUID = -3708962977537779461L;

    private final List<ObjectError> errors;

    public LoginValidationException(List<ObjectError> errors) {
        super("Invalid Login Inputs. Please fix the following errors");
        this.errors = errors;
    }

    public List<ObjectError> getErrorsList() {
        return errors;
    }
}