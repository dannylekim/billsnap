package proj.kedabra.billsnap;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.presentation.ApiError;

@RestControllerAdvice
public class BillSnapExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(FieldValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ApiError handleFieldValidation(final FieldValidationException ex) {
        return new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrors());

    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ApiError handleIllegalArgument(final IllegalArgumentException ex) {
        return new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ApiError handleIllegalState(final IllegalStateException ex) {
        return new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ApiError handleResourceNotFound(final ResourceNotFoundException ex) {
        return new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccessForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    protected ApiError handleHttpAccessForbidden(final AccessForbiddenException ex) {
        return new ApiError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(FunctionalWorkflowException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    protected ApiError handleFunctionalWorkflow(final FunctionalWorkflowException ex) {
        return new ApiError(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ApiError handleUnknownException(final Exception ex) {
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error occurred. Please try again later");
    }


}
