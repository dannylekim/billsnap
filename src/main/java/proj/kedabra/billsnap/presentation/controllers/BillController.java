package proj.kedabra.billsnap.presentation.controllers;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.BillCreationResource;
import proj.kedabra.billsnap.presentation.resources.BillResource;

@RestController
public class BillController {

    private BillMapper billMapper;

    private BillFacade billFacade;

    public BillController(final BillMapper billMapper, final BillFacade billFacade) {
        this.billMapper = billMapper;
        this.billFacade = billFacade;
    }


    @PostMapping("/bills")
    @ApiOperation(value = "Add personal bill", notes = "Add a personal bill to a user account.")
    @ApiResponses({
            @ApiResponse(code = 201, response = BillResource.class, message = "Successfully added a bill!"),
            @ApiResponse(code = 400, response = ApiError.class, message = "Cannot create bill with wrong inputs."),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.CREATED)
    public BillResource createBill(@ApiParam(required = true, name = "Bill Details", value = "Minimum bill details")
                                   @RequestBody @Valid final BillCreationResource billCreationResource,
                                   @AuthenticationPrincipal Principal principal,
                                   BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final BillDTO billDTO = billMapper.toDTO(billCreationResource);
        final BillDTO createdBill = billFacade.addPersonalBill(principal.getName(), billDTO);
        return billMapper.toResource(createdBill);


    }

}
