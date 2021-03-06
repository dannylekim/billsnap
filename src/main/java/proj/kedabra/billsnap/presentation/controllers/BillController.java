package proj.kedabra.billsnap.presentation.controllers;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.AssociateBillResource;
import proj.kedabra.billsnap.presentation.resources.BillCreationResource;
import proj.kedabra.billsnap.presentation.resources.BillResource;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;
import proj.kedabra.billsnap.presentation.resources.EditBillResource;
import proj.kedabra.billsnap.presentation.resources.InviteRegisteredResource;
import proj.kedabra.billsnap.presentation.resources.StartBillResource;
import proj.kedabra.billsnap.utils.CacheNames;

@RestController
public class BillController {

    private final BillMapper billMapper;

    private final BillFacade billFacade;

    public BillController(final BillMapper billMapper, final BillFacade billFacade) {
        this.billMapper = billMapper;
        this.billFacade = billFacade;
    }

    @CacheEvict(value = {CacheNames.BILL, CacheNames.PAYMENTS}, key = "#principal.name")
    @PostMapping("/bills")
    @Operation(summary = "Add personal bill", description = "Add a personal bill to a user account.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = BillResource.class)), description = "Successfully added a bill!")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Cannot create bill with wrong inputs.")
    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource.")
    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource.")
    @ResponseStatus(HttpStatus.CREATED)
    public BillResource createBill(@Parameter(required = true, name = "Bill Details", description = "Minimum bill details")
                                   @RequestBody @Valid final BillCreationResource billCreationResource,
                                   final BindingResult bindingResult,
                                   @AuthenticationPrincipal final Principal principal) {

        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final BillDTO billDTO = billMapper.toBillDTO(billCreationResource);
        final BillCompleteDTO createdBill = billFacade.addPersonalBill(principal.getName(), billDTO);
        return billMapper.toResource(createdBill);
    }

    @Cacheable(value = CacheNames.BILL, key = "#billId")
    @GetMapping("/bills/{billId}")
    @Operation(summary = "Get detailed bill", description = "Get detailed bill associated to account")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully retrieved detailed bill!")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "No bill with that id exists")
    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Access is unauthorized!")
    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Account does not have the bill specified.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#billId)")
    public BillSplitResource getDetailedBill(
            @AuthenticationPrincipal final Principal principal,
            @Parameter(required = true, name = "billId", description = "bill ID")
            @PathVariable("billId") final Long billId) {
        final BillSplitDTO detailedBill = billFacade.getDetailedBill(billId);
        return billMapper.toResource(detailedBill);
    }

    @CachePut(value = CacheNames.BILL, key = "#associateBillResource.id")
    @CacheEvict(value = CacheNames.PAYMENTS, key = "#principal.name")
    @PutMapping("/bills")
    @Operation(summary = "Associate users/modify bill", description = "Modify bill's users/items and user-item association")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully modified bill!")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Error modifying bill")
    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource.")
    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource.")
    @ApiResponse(responseCode = "405", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "The bill is not in Open status.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('RESPONSIBLE_' + #associateBillResource.id)")
    public BillSplitResource modifyBill(@Parameter(required = true, name = "Bill modification details", description = "Minimum bill modification details")
                                        @RequestBody @Valid final AssociateBillResource associateBillResource,
                                        final BindingResult bindingResult,
                                        @AuthenticationPrincipal final Principal principal) {

        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final AssociateBillDTO associateBill = billMapper.toAssociateBillDTO(associateBillResource);
        final BillSplitDTO billSplit = billFacade.associateAccountsToBill(associateBill);
        return billMapper.toResource(billSplit);
    }

    @CachePut(value = CacheNames.BILL, key = "#billId")
    @PostMapping("bills/{billId}/accounts")
    @Operation(summary = "Invite registered users to bill", description = "Sends notification invite to all registered users in given list")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully invited Registered users to bill!")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Error inviting registered users to bill.")
    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource.")
    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource.")
    @ApiResponse(responseCode = "405", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "The bill is not in Open status.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('RESPONSIBLE_' + #billId)")
    public BillSplitResource inviteRegisteredToBill(@Parameter(required = true, name = "billId", description = "bill ID")
                                                    @PathVariable("billId") final Long billId,
                                                    @Parameter(required = true, name = "List of emails to invite", description = "List of emails to invite")
                                                    @RequestBody @Valid final InviteRegisteredResource inviteRegisteredResource,
                                                    final BindingResult bindingResult,
                                                    @AuthenticationPrincipal final Principal principal) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final var pendingRegisteredBillSplitDTO = billFacade.inviteRegisteredToBill(billId, inviteRegisteredResource.getAccounts());
        return billMapper.toResource(pendingRegisteredBillSplitDTO);
    }

    @CachePut(value = CacheNames.BILL, key = "#startBillResource.id")
    @CacheEvict(value = CacheNames.PAYMENTS, key = "#principal.name")
    @PostMapping("bills/start")
    @Operation(summary = "Start a bill", description = "Blocks all modifications on started bill")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully started bill!")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Bill doesn't exist")
    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource.")
    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource.")
    @ApiResponse(responseCode = "405", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "The bill is not in Open status.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('RESPONSIBLE_' + #startBillResource.id)")
    public BillSplitResource startBill(@Parameter(required = true, name = "id of bill", description = "id of bill")
                                       @RequestBody @Valid final StartBillResource startBillResource,
                                       final BindingResult bindingResult,
                                       @AuthenticationPrincipal final Principal principal) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final var billSplitDTO = billFacade.startBill(startBillResource.getId());
        return billMapper.toResource(billSplitDTO);
    }

    @CachePut(value = CacheNames.BILL, key = "#billId")
    @CacheEvict(value = CacheNames.PAYMENTS, key = "#principal.name")
    @PutMapping("bills/{billId}")
    @Operation(summary = "Edit bill", description = "Edit an unstarted bill")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully edited bill!")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Bill doesn't exist. \t\n" +
            "Bill already started. \t\n" +
            "Item doesn't exists. \t\n" +
            "Account does not have the bill specified. \t\n" +
            "Only one type of tipping is supported. Please make sure only either tip amount or tip percent is set.")
    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "The user making the request is not the Bill responsible.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('RESPONSIBLE_' + #billId)")
    public BillSplitResource editBill(@Parameter(required = true, name = "billId", description = "bill ID")
                                      @PathVariable("billId") final Long billId,
                                      @RequestBody @Valid final EditBillResource editBillResource,
                                      final BindingResult bindingResult,
                                      @AuthenticationPrincipal final Principal principal) {

        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final var editBillDTO = billMapper.toDTO(editBillResource);
        final var billSplitDTO = billFacade.editBill(billId, principal.getName(), editBillDTO);

        return billMapper.toResource(billSplitDTO);
    }
}
