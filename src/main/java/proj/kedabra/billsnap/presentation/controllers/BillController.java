package proj.kedabra.billsnap.presentation.controllers;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
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
import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import springfox.documentation.annotations.ApiIgnore;

import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.config.SwaggerConfiguration;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.AssociateBillResource;
import proj.kedabra.billsnap.presentation.resources.BillCreationResource;
import proj.kedabra.billsnap.presentation.resources.BillResource;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;

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
            @ApiResponse(code = 201, response = BillResource.class, message = "Successfully retrieved all bills!"),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.CREATED)
    public List<BillResource> getAllBills(@ApiIgnore @AuthenticationPrincipal final Principal principal) {

        final List<BillCompleteDTO> billsFromEmail = billFacade.getAllBillsByEmail(principal.getName());
        return billsFromEmail.stream().map(billMapper::toResource).collect(Collectors.toList());

    }

    @PutMapping("/bills")
    @ApiOperation(value = "Associate users/modify bill", notes = "Modify bill's users/items and user-item association",
            authorizations = {@Authorization(value = SwaggerConfiguration.API_KEY)})
    @ApiResponses({
            @ApiResponse(code = 200, response = BillSplitResource.class, message = "Successfully modified bill!"),
            @ApiResponse(code = 400, response = ApiError.class, message = "Error modifying bill"),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
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
}
