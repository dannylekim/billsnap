package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AssociateBillResource implements Serializable {

    private static final long serialVersionUID = 6080802006414641448L;

    @NotBlank
    @ApiModelProperty(name = "Bill id to modify")
    private Long id;

    @NotNull
    @ApiModelProperty(name = "list of items to associate to the account", position = 1)
    private List<@Valid ItemAssociationResource> itemsPerAccount;

}


