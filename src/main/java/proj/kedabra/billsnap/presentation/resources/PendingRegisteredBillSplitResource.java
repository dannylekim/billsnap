package proj.kedabra.billsnap.presentation.resources;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PendingRegisteredBillSplitResource  extends BillSplitResource{

    @ApiModelProperty(name = "List of emails whose invitation status is pending", position = 13)
    private List<String> pendingAccounts;

}
