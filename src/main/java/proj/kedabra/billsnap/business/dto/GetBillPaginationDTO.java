package proj.kedabra.billsnap.business.dto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;

import lombok.Getter;
import lombok.Setter;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;

@Getter
@Setter
public class GetBillPaginationDTO {

    private String email;

    private List<BillStatusEnum> statuses;

    private InvitationStatusEnum invitationStatus;

    private ZonedDateTime startDate;

    private ZonedDateTime endDate;

    private String category;

    private Pageable pageable;

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate.atStartOfDay().atZone(ZoneId.systemDefault());
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate.atStartOfDay().atZone(ZoneId.systemDefault());
    }


}
