package proj.kedabra.billsnap.presentation.controllers;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.facade.AccountFacade;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.AccountCreationResource;
import proj.kedabra.billsnap.presentation.resources.AccountResource;
import proj.kedabra.billsnap.utils.annotations.ObfuscateArgs;


@RestController
public class AccountController {


    private final AccountFacade accountFacade;

    private final AccountMapper mapper;

    public AccountController(final AccountFacade accountFacade, AccountMapper mapper) {
        this.accountFacade = accountFacade;
        this.mapper = mapper;
    }


    @PostMapping("/register")
    @ApiOperation(value = "Register Account", notes = "Register an account in the application using minimum details")
    @ApiResponses({
            @ApiResponse(code = 201, response = AccountResource.class, message = "Successfully registered an account!"),
            @ApiResponse(code = 400, response = ApiError.class, message = "Cannot register account with wrong inputs."),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.CREATED)
    @ObfuscateArgs
    public AccountResource createAccount(@ApiParam(required = true, name = "Registration Details", value = "Minimum registration details")
                                         @RequestBody @Valid AccountCreationResource accountCreationResource,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        AccountDTO newCreationDTO = mapper.toDTO(accountCreationResource);
        AccountDTO accountDTO = accountFacade.registerAccount(newCreationDTO);

        return mapper.toResource(accountDTO);


    }


}
