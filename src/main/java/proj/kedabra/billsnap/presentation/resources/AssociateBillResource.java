package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AssociateBillResource implements Serializable {

    @NotNull
    @Schema(description = "Bill id to modify")
    private Long id;

    @NotNull
    @Schema(description = "list of items to associate to the account")
    private List<@Valid ItemAssociationResource> itemsPerAccount;

}


