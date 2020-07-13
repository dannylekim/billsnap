package proj.kedabra.billsnap.business.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;

import lombok.Getter;
import lombok.Setter;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

@Getter
@Setter
public class GetBillPaginationDTO {

    private String email;

    private List<BillStatusEnum> statuses;

    private LocalDate startDate;

    private LocalDate endDate;

    private String category;

    private Pageable pageable;

}
