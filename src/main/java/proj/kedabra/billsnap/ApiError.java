package proj.kedabra.billsnap;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import lombok.Data;

@Data
public class ApiError {

    private final HttpStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private final ZonedDateTime timestamp = ZonedDateTime.now();

    private final String message;

    private List<ApiSubError> errors = new ArrayList<>();

    public ApiError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiError(HttpStatus status, String message, List<ObjectError> errors) {
        this(status, message);
        this.errors = errors.stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .map(ApiSubError::new)
                .collect(Collectors.toList());
    }


}
