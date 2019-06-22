package proj.kedabra.billsnap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import proj.kedabra.billsnap.business.exception.FieldValidationException;

@ControllerAdvice
public class BillSnapExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(FieldValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<ApiError> handleFieldValidation(final FieldValidationException ex) {
        var error = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrors());
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<ApiError> handleUnknownException(final Exception ex) {
        var error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error occurred. Please try again later");
        return new ResponseEntity<>(error, error.getStatus());
    }
}
