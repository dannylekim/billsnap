package proj.kedabra.billsnap.fixtures;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import proj.kedabra.billsnap.business.dto.GetBillPaginationDTO;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.presentation.resources.OrderByEnum;
import proj.kedabra.billsnap.presentation.resources.SortByEnum;

public final class GetBillPaginationDTOFixture {

    private GetBillPaginationDTOFixture() {}

    public static GetBillPaginationDTO getDefault() {
        final var billPagination = new GetBillPaginationDTO();

        billPagination.setEmail("billPagination@email.com");
        final LocalDate startDate = LocalDate.of(2019, Month.JANUARY, 1);
        billPagination.setStartDate(startDate);

        final LocalDate endDate = LocalDate.of(2020, Month.JANUARY, 1);
        billPagination.setEndDate(endDate);
        billPagination.setCategory("restaurant");

        final List<BillStatusEnum> statuses = new ArrayList<>();
        statuses.add(BillStatusEnum.OPEN);
        statuses.add(BillStatusEnum.RESOLVED);
        statuses.add(BillStatusEnum.IN_PROGRESS);
        billPagination.setStatuses(statuses);

        final List<SortByEnum> sortByList = new ArrayList<>();
        sortByList.add(SortByEnum.CREATED);

        final var sort = Sort.by(Sort.Direction.fromString(OrderByEnum.ASC.name()), sortByList.stream().map(Enum::name).map(String::toLowerCase).toArray(String[]::new));
        final Pageable pageable = PageRequest.of(0, 2, sort);
        billPagination.setPageable(pageable);

        return billPagination;
    }

    public static GetBillPaginationDTO getCustom(String category, OrderByEnum orderBy, List<SortByEnum> sortByList, int page, int size) {
        final var billPagination = new GetBillPaginationDTO();

        billPagination.setEmail("billPagination@email.com");

        final LocalDate startDate = LocalDate.of(2019, Month.JANUARY, 1);
        billPagination.setStartDate(startDate);

        final LocalDate endDate = LocalDate.of(2019, Month.DECEMBER, 31);
        billPagination.setEndDate(endDate);

        final List<BillStatusEnum> statuses = new ArrayList<>();
        statuses.add(BillStatusEnum.OPEN);
        statuses.add(BillStatusEnum.RESOLVED);
        statuses.add(BillStatusEnum.IN_PROGRESS);
        billPagination.setStatuses(statuses);

        billPagination.setCategory(category);

        final var sort = Sort.by(Sort.Direction.fromString(orderBy.name()), sortByList.stream().map(Enum::name).map(String::toLowerCase).toArray(String[]::new));
        final Pageable pageable = PageRequest.of(page, size, sort);
        billPagination.setPageable(pageable);

        return billPagination;
    }

}
