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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import springfox.documentation.annotations.ApiIgnore;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.config.SwaggerConfiguration;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.AssociateBillResource;
import proj.kedabra.billsnap.presentation.resources.BillCreationResource;
import proj.kedabra.billsnap.presentation.resources.BillResource;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;
import proj.kedabra.billsnap.presentation.resources.InviteRegisteredResource;
import proj.kedabra.billsnap.presentation.resources.PendingRegisteredBillSplitResource;
import proj.kedabra.billsnap.presentation.resources.ShortBillResource;

@RestController
public class BillController {

    private BillMapper billMapper;

    private BillFacade billFacade;

    public BillController(final BillMapper billMapper, final BillFacade billFacade) {
        this.billMapper = billMapper;
        this.billFacade = billFacade;
    }


    @PostMapping("/bills")
    @ApiOperation(value = "Add personal bill", notes = "Add a personal bill to a user account.",
            authorizations = {@Authorization(value = SwaggerConfiguration.API_KEY)})
    @ApiResponses({
            @ApiResponse(code = 201, response = BillResource.class, message = "Successfully added a bill!"),
            @ApiResponse(code = 400, response = ApiError.class, message = "Cannot create bill with wrong inputs."),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.CREATED)
    public BillResource createBill(@ApiParam(required = true, name = "Bill Details", value = "Minimum bill details")
                                   @RequestBody @Valid final BillCreationResource billCreationResource,
                                   final BindingResult bindingResult,
                                   @ApiIgnore @AuthenticationPrincipal final Principal principal) {

        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final BillDTO billDTO = billMapper.toBillDTO(billCreationResource);
        final BillCompleteDTO createdBill = billFacade.addPersonalBill(principal.getName(), billDTO);
        return billMapper.toResource(createdBill);
    }

    @GetMapping("/bills")
    @ApiOperation(value = "Get all bills", notes = "Get all bills associated to an account",
            authorizations = {@Authorization(value = SwaggerConfiguration.API_KEY)})
    @ApiResponses({
            @ApiResponse(code = 200, response = ShortBillResource.class, message = "Successfully retrieved all bills!"),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.OK)
    public List<ShortBillResource> getAllBills(@ApiIgnore @AuthenticationPrincipal final Principal principal) {

        final List<BillSplitDTO> billsFromEmail = billFacade.getAllBillsByEmail(principal.getName());
        return billsFromEmail.stream().map(billMapper::toShortBillResource).collect(Collectors.toList());

    }

    @GetMapping("/bills/{billId}")
    @ApiOperation(value = "Get detailed bill", notes = "Get detailed bill associated to account",
            authorizations = {@Authorization(value = SwaggerConfiguration.API_KEY)})
    @ApiResponses({
            @ApiResponse(code = 200, response = BillSplitResource.class, message = "Successfully retrieved detailed bill!"),
            @ApiResponse(code = 400, response = ApiError.class, message = "No bill with that id exists"),
            @ApiResponse(code = 401, response = ApiError.class, message = "Access is unauthorized!"),
            @ApiResponse(code = 403, response = ApiError.class, message = "Account does not have the bill specified."),
    })
    @ResponseStatus(HttpStatus.OK)
    public BillSplitResource getDetailedBill(@ApiIgnore
                                             @AuthenticationPrincipal final Principal principal,
                                             @ApiParam(required = true, name = "billId", value = "bill ID")
                                             @PathVariable("billId") final Long billId) {
        final BillSplitDTO detailedBill = billFacade.getDetailedBill(billId, principal.getName());
        return billMapper.toResource(detailedBill);
    }

    @PutMapping("/bills")
    @ApiOperation(value = "Associate users/modify bill", notes = "Modify bill's users/items and user-item association",
            authorizations = {@Authorization(value = SwaggerConfiguration.API_KEY)})
    @ApiResponses({
            @ApiResponse(code = 200, response = BillSplitResource.class, message = "Successfully modified bill!"),
            @ApiResponse(code = 400, response = ApiError.class, message = "Error modifying bill"),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
            @ApiResponse(code = 405, response = ApiError.class, message = "The bill is not in Open status."),
    })
    @ResponseStatus(HttpStatus.OK)
    public BillSplitResource modifyBill(@ApiParam(required = true, name = "Bill modification details", value = "Minimum bill modification details")
                                        @RequestBody @Valid final AssociateBillResource associateBillResource,
                                        final BindingResult bindingResult,
                                        @ApiIgnore @AuthenticationPrincipal final Principal principal) {

        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final AssociateBillDTO associateBill = billMapper.toAssociateBillDTO(associateBillResource);
        final BillSplitDTO billSplit = billFacade.associateAccountsToBill(associateBill);
        return billMapper.toResource(billSplit);
    }

    @PostMapping("bills/{billId}/accounts")
    @ApiOperation(value = "Invite registered users to bill", notes = "Sends notification invite to all registered users in given list",
            authorizations = {@Authorization(value = SwaggerConfiguration.API_KEY)})
    @ApiResponses({
            @ApiResponse(code = 200, response = PendingRegisteredBillSplitResource.class, message = "Successfully invited Registered users to bill!"),
            @ApiResponse(code = 400, response = ApiError.class, message = "Error inviting registered users to bill."),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
            @ApiResponse(code = 405, response = ApiError.class, message = "The bill is not in Open status."),
    })
    @ResponseStatus(HttpStatus.OK)
    public PendingRegisteredBillSplitResource inviteRegisteredToBill(@ApiParam(required = true, name = "billId", value = "bill ID")
                                                                     @PathVariable("billId") final Long billId,
                                                                     @ApiParam(required = true, name = "List of emails to invite", value = "List of emails to invite")
                                                                     @RequestBody @Valid final InviteRegisteredResource inviteRegisteredResource,
                                                                     final BindingResult bindingResult,
                                                                     @ApiIgnore @AuthenticationPrincipal final Principal principal) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final var pendingRegisteredBillSplitDTO = billFacade.inviteRegisteredToBill(billId, principal.getName(), inviteRegisteredResource.getAccounts());
        return billMapper.toResource(pendingRegisteredBillSplitDTO);
    }

}
