package proj.kedabra.billsnap.presentation.controllers;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.facade.AccountFacade;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.AccountCreationResource;
import proj.kedabra.billsnap.presentation.resources.AccountResource;
import proj.kedabra.billsnap.presentation.resources.LoginResource;
import proj.kedabra.billsnap.presentation.resources.LoginResponseResource;
import proj.kedabra.billsnap.utils.annotations.ObfuscateArgs;


@RestController
public class AccountController {


    private final AccountFacade accountFacade;

    private final AccountMapper mapper;

    public AccountController(final AccountFacade accountFacade, final AccountMapper mapper) {
        this.accountFacade = accountFacade;
        this.mapper = mapper;
    }


    @PostMapping("/register")
    @Operation(summary = "Register Account", description = "Register an account in the application using minimum details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = AccountResource.class)), description = "Successfully registered an account!"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Cannot register account with wrong inputs."),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource."),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.CREATED)
    @ObfuscateArgs
    @SecurityRequirements
    public AccountResource createAccount(@Parameter(required = true, name = "Registration Details", description = "Minimum registration details")
                                         @RequestBody @Valid final AccountCreationResource accountCreationResource,
                                         final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        AccountDTO newCreationDTO = mapper.toDTO(accountCreationResource);
        AccountDTO accountDTO = accountFacade.registerAccount(newCreationDTO);

        return mapper.toResource(accountDTO);


    }

    @PostMapping(path = "/login", consumes = "application/json")
    @Operation(summary = "Login", description = "Login to the application")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LoginResponseResource.class)), description = "Successfully logged in."),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Cannot login with invalid inputs."),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Cannot login with incorrect credentials."),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseResource loginAccount(@Parameter(required = true, name = "Login Details", description = "Valid login details")
                                              @RequestBody @Valid LoginResource loginResource) {
        throw new IllegalStateException("loginAccount() shouldn't be called from Controller: it is implemented by custom AuthenticationFilter.");
    }

}
