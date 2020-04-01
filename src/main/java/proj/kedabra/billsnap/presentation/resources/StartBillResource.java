package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StartBillResource implements Serializable {

    private static final long serialVersionUID = -9069248187666878076L;

    @NotNull
    @ApiModelProperty(name = "id of bill to Start")
    private Long id;

}
