package proj.kedabra.billsnap.business.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.Errors;

public class LoginValidationException extends AuthenticationException {

    private static final long serialVersionUID = -3708962977537779461L;

    private final Errors errors;

    public LoginValidationException(Errors errors) {
        super("Invalid Login Inputs. Please fix the following errors");
        this.errors = errors;
    }

    public Errors getErrorsList() {
        return errors;
    }
}