package proj.kedabra.billsnap.business.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PaymentsOwedDTO {

    private List<AmountsOwedDTO> amountsOwedList = new ArrayList<>();

}
