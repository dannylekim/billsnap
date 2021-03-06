package proj.kedabra.billsnap.presentation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiError {

    @Schema(description = "Http status of the error")
    private HttpStatus status;

    @Schema(description = "Time of the error")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now(ZoneId.systemDefault());

    @Schema(description = "The error message")
    private String message;

    @Schema(description = "The list of Sub errors if they exist. Generally only for validation")
    private List<ApiSubError> errors = new ArrayList<>();

    /**
     * Set a timestamp from a string with the format dd-MM-yyyy HH:mm:ss. Mostly used for testing purposes.
     *
     * @param timestamp String of format dd-MM-yyyy HH:mm:ss
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    /**
     * Do not use this constructor. It is solely for mapping/testing purposes
     */
    public ApiError() {
    }

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
