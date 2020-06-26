package proj.kedabra.billsnap.presentation.resources;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PendingRegisteredBillSplitResource  extends BillSplitResource{

    @Schema(description = "List of emails whose invitation status is pending")
    private List<String> pendingAccounts;

}
