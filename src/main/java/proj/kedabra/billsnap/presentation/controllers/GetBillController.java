package proj.kedabra.billsnap.presentation.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Range;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.GetBillPaginationDTO;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.OrderByEnum;
import proj.kedabra.billsnap.presentation.resources.ShortBillResource;
import proj.kedabra.billsnap.presentation.resources.SortByEnum;
import proj.kedabra.billsnap.utils.CacheNames;

@RestController
@Validated
public class GetBillController {

    private final BillMapper billMapper;

    private final BillFacade billFacade;

    public GetBillController(final BillMapper billMapper, final BillFacade billFacade) {
        this.billMapper = billMapper;
        this.billFacade = billFacade;
    }

    @Cacheable(value = CacheNames.BILLS)
    @GetMapping("/bills")
    @Operation(summary = "Get all bills", description = "Get all bills associated to an account")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all bills!")
    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource.")
    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource.")
    @ResponseStatus(HttpStatus.OK)
    public List<ShortBillResource> getAllBills(
            @RequestParam(value = "statuses", defaultValue = "OPEN, IN_PROGRESS, RESOLVED")
            @NotEmpty(message = "Can not have empty list of statuses") final List<BillStatusEnum> statuses,
            @RequestParam(value = "start", defaultValue = "1970-01-01")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @RequestParam(value = "end", defaultValue = "9999-12-31")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate,
            @RequestParam(value = "page_size", defaultValue = "100")
            @Range(message = "the number must be positive") final int pageSize,
            @RequestParam(value = "page_number", defaultValue = "0")
            @Range(message = "the number must be positive") final int pageNumber,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "sort_by", defaultValue = "CREATED, STATUS")
            @NotEmpty(message = "Can not have empty list of sort by") final List<SortByEnum> sortBy,
            @RequestParam(value = "order_by", defaultValue = "DESC") final OrderByEnum orderBy,
            @AuthenticationPrincipal final Principal principal) {

        final var billPaginationDTO = new GetBillPaginationDTO();
        billPaginationDTO.setEmail(principal.getName());
        billPaginationDTO.setStatuses(statuses);
        billPaginationDTO.setStartDate(startDate);
        billPaginationDTO.setEndDate(endDate);
        billPaginationDTO.setCategory(category);
        final var sort = Sort.by(Sort.Direction.fromString(orderBy.name()), sortBy.stream().map(Enum::name).map(String::toLowerCase).toArray(String[]::new));
        final var pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        billPaginationDTO.setPageable(pageRequest);

        final List<BillSplitDTO> billsFromEmail = billFacade.getAllBillsByEmailPageable(billPaginationDTO);
        return billsFromEmail.stream().map(billMapper::toShortBillResource).collect(Collectors.toList());

    }

}
