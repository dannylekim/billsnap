package proj.kedabra.billsnap.presentation.controllers;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
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
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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
import proj.kedabra.billsnap.presentation.resources.ShortBillResource;
import proj.kedabra.billsnap.presentation.resources.StartBillResource;

@RestController
public class BillController {

    private final BillMapper billMapper;

    private final BillFacade billFacade;

    public BillController(final BillMapper billMapper, final BillFacade billFacade) {
        this.billMapper = billMapper;
        this.billFacade = billFacade;
    }


    @PostMapping("/bills")
    @Operation(summary = "Add personal bill", description = "Add a personal bill to a user account.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = BillResource.class)), description = "Successfully added a bill!"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Cannot create bill with wrong inputs."),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource."),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource."),
    })
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

    @GetMapping("/bills")
    @Operation(summary = "Get all bills", description = "Get all bills associated to an account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all bills!"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource."),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.OK)
    public List<ShortBillResource> getAllBills(@AuthenticationPrincipal final Principal principal) {

        final List<BillSplitDTO> billsFromEmail = billFacade.getAllBillsByEmail(principal.getName());
        return billsFromEmail.stream().map(billMapper::toShortBillResource).collect(Collectors.toList());

    }

    @GetMapping("/bills/{billId}")
    @Operation(summary = "Get detailed bill", description = "Get detailed bill associated to account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully retrieved detailed bill!"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "No bill with that id exists"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Access is unauthorized!"),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Account does not have the bill specified."),
    })
    @ResponseStatus(HttpStatus.OK)
    public BillSplitResource getDetailedBill(
            @AuthenticationPrincipal final Principal principal,
            @Parameter(required = true, name = "billId", description = "bill ID")
            @PathVariable("billId") final Long billId) {
        final BillSplitDTO detailedBill = billFacade.getDetailedBill(billId, principal.getName());
        return billMapper.toResource(detailedBill);
    }

    @PutMapping("/bills")
    @Operation(summary = "Associate users/modify bill", description = "Modify bill's users/items and user-item association")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully modified bill!"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Error modifying bill"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource."),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource."),
            @ApiResponse(responseCode = "405", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "The bill is not in Open status."),
    })
    @ResponseStatus(HttpStatus.OK)
    public BillSplitResource modifyBill(@Parameter(required = true, name = "Bill modification details", description = "Minimum bill modification details")
                                        @RequestBody @Valid final AssociateBillResource associateBillResource,
                                        final BindingResult bindingResult,
                                        @AuthenticationPrincipal final Principal principal) {

        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final AssociateBillDTO associateBill = billMapper.toAssociateBillDTO(associateBillResource);
        final BillSplitDTO billSplit = billFacade.associateAccountsToBill(associateBill, principal.getName());
        return billMapper.toResource(billSplit);
    }

    @PostMapping("bills/{billId}/accounts")
    @Operation(summary = "Invite registered users to bill", description = "Sends notification invite to all registered users in given list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully invited Registered users to bill!"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Error inviting registered users to bill."),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource."),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource."),
            @ApiResponse(responseCode = "405", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "The bill is not in Open status."),
    })
    @ResponseStatus(HttpStatus.OK)
    public BillSplitResource inviteRegisteredToBill(@Parameter(required = true, name = "billId", description = "bill ID")
                                                    @PathVariable("billId") final Long billId,
                                                    @Parameter(required = true, name = "List of emails to invite", description = "List of emails to invite")
                                                    @RequestBody @Valid final InviteRegisteredResource inviteRegisteredResource,
                                                    final BindingResult bindingResult,
                                                    @AuthenticationPrincipal final Principal principal) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final var pendingRegisteredBillSplitDTO = billFacade.inviteRegisteredToBill(billId, principal.getName(), inviteRegisteredResource.getAccounts());
        return billMapper.toResource(pendingRegisteredBillSplitDTO);
    }

    @PostMapping("bills/start")
    @Operation(summary = "Start a bill", description = "Blocks all modifications on started bill")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully started bill!"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Bill doesn't exist"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource."),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource."),
            @ApiResponse(responseCode = "405", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "The bill is not in Open status."),
    })
    @ResponseStatus(HttpStatus.OK)
    public BillSplitResource startBill(@Parameter(required = true, name = "id of bill", description = "id of bill")
                                       @RequestBody @Valid final StartBillResource startBillResource,
                                       final BindingResult bindingResult,
                                       @AuthenticationPrincipal final Principal principal) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final var billSplitDTO = billFacade.startBill(startBillResource.getId(), principal.getName());
        return billMapper.toResource(billSplitDTO);
    }

    @PutMapping("bills/{billId}")
    @Operation(summary = "Edit bill", description = "Edit an unstarted bill")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully edited bill!"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Bill doesn't exist. \t\n" +
                    "Bill already started. \t\n" +
                    "Item doesn't exists. \t\n" +
                    "Account does not have the bill specified. \t\n" +
                    "Only one type of tipping is supported. Please make sure only either tip amount or tip percent is set."),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "The user making the request is not the Bill responsible."),
    })
    @ResponseStatus(HttpStatus.OK)
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
