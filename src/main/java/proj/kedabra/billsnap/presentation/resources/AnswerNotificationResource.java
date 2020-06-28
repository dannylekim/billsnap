package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnswerNotificationResource implements Serializable {

    @NotNull
    @Schema(description = "Answer (Yes/No) to a bill invitation")
    private Boolean answer;

}
