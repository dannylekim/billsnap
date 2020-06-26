package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StartBillResource implements Serializable {

    @NotNull
    @Schema(description = "id of bill to Start")
    private Long id;

}
