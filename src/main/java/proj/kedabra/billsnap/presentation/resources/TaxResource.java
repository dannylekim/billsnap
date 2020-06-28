package proj.kedabra.billsnap.presentation.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaxResource extends NewTaxResource {

    @Schema(description = "Tax id if it exists")
    private Long id;


}
