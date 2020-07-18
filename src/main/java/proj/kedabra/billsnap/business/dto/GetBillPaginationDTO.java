package proj.kedabra.billsnap.business.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    private ZonedDateTime startDate;

    private ZonedDateTime endDate;

    private String category;

    private Pageable pageable;

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate.atZone(ZoneId.systemDefault());
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate.atZone(ZoneId.systemDefault());
    }


}
