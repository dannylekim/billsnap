package proj.kedabra.billsnap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.fixtures.FieldErrorFixture;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.ApiSubError;

class BillSnapExceptionHandlerTest {

    private BillSnapExceptionHandler billSnapExceptionHandler = new BillSnapExceptionHandler();

    private static final String NOT_THIS_ERROR_MESSAGE = "NOT THIS ERROR MESSAGE";


    @Test
    @DisplayName("Should return ResponseEntity with FieldValidation")
    void shouldReturnFieldValidationResponseEntity() {
        //Given
        final var fieldError = FieldErrorFixture.getDefault(0);
        final var ex = new FieldValidationException(List.of(fieldError));

        //When
        final ApiError response = billSnapExceptionHandler.handleFieldValidation(ex);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(ex.getMessage(), response.getMessage());
        assertEquals(new ApiSubError(fieldError), response.getErrors().get(0));
    }

    @Test
    @DisplayName("Should return ResponseEntity with error 500 and unknown error message")
    void shouldReturnUnknownResponseEntity() {
        //Given
        final var ex = new UnsupportedOperationException(NOT_THIS_ERROR_MESSAGE);

        //When
        final ApiError response = billSnapExceptionHandler.handleUnknownException(ex);

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Unknown error occurred. Please try again later", response.getMessage());
    }

    @Test
    @DisplayName("Should return ResponseEntity with error 400 and IllegalArgument's message")
    void shouldReturn400ForIllegalArgumentException() {
        //Given
        final var ex = new IllegalArgumentException(NOT_THIS_ERROR_MESSAGE);

        //When
        final ApiError response = billSnapExceptionHandler.handleIllegalArgument(ex);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(NOT_THIS_ERROR_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Should return ResponseEntity with error 400 and IllegalState's message")
    void shouldReturn400ForIllegalStateException() {
        //Given
        final var ex = new IllegalStateException(NOT_THIS_ERROR_MESSAGE);

        //When
        final ApiError response = billSnapExceptionHandler.handleIllegalState(ex);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(NOT_THIS_ERROR_MESSAGE, response.getMessage());
    }



    @Test
    @DisplayName("Should return ResponseEntity with error 400 and ResourceNotFound's message")
    void shouldReturn404ForResourceNotFoundException() {
        //Given
        final var ex = new ResourceNotFoundException(NOT_THIS_ERROR_MESSAGE);

        //When
        final ApiError response = billSnapExceptionHandler.handleResourceNotFound(ex);

        //Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertEquals(NOT_THIS_ERROR_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Should return ResponseEntity with error 403 and AccessForbidden's message")
    void shouldReturn403ForAccessForbiddenException() {
        //Given
        final var ex = new AccessForbiddenException(NOT_THIS_ERROR_MESSAGE);

        //When
        final ApiError response = billSnapExceptionHandler.handleHttpAccessForbidden(ex);

        //Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatus());
        assertEquals(NOT_THIS_ERROR_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Should return ResponseEntity with error 405 and FunctionalWorkflow's message")
    void shouldReturn405ForFunctionalWorkflowException() {
        //Given
        final var ex = new FunctionalWorkflowException(NOT_THIS_ERROR_MESSAGE);

        //When
        final ApiError response = billSnapExceptionHandler.handleFunctionalWorkflow(ex);

        //Then
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
        assertEquals(NOT_THIS_ERROR_MESSAGE, response.getMessage());
    }


}