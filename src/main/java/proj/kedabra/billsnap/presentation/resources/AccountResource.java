package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class AccountResource extends BaseAccountResource implements Serializable {

    @Schema(description = "Unique ID of the user")
    private Long id;

    @Schema(description = "Email of the user")
    private String email;

}
