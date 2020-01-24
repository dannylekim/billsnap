package proj.kedabra.billsnap.presentation.controllers;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import springfox.documentation.annotations.ApiIgnore;

import proj.kedabra.billsnap.business.dto.PaymentInformationDTO;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.facade.PaymentFacade;
import proj.kedabra.billsnap.config.SwaggerConfiguration;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.PaymentResource;
import proj.kedabra.billsnap.presentation.resources.RemainingPaymentResource;

@RestController
@RequestMapping("/resolve/bills")
public class ResolveBillController {

    private final PaymentFacade paymentFacade;

    public ResolveBillController(final PaymentFacade paymentFacade) {
        this.paymentFacade = paymentFacade;
    }

    @PostMapping
    @ApiOperation(value = "Add a bill", notes = "Pay a personal bill.", authorizations = {@Authorization(value = SwaggerConfiguration.API_KEY)})
    @ApiResponses({
            @ApiResponse(code = 200, response = RemainingPaymentResource.class, message = "You've successfully paid a bill!"),
            @ApiResponse(code = 400, response = ApiError.class, message = "Cannot pay a bill with wrong inputs."),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.OK)
    public RemainingPaymentResource payBill(@RequestBody @Valid @ApiParam(required = true, name = "Payment details", value = "Amount paid to bill") final PaymentResource payment,
                                            final BindingResult bindingResult,
                                            @ApiIgnore @AuthenticationPrincipal Principal principal) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final var email = principal.getName();
        final var paymentInformationDTO = new PaymentInformationDTO();
        paymentInformationDTO.setAmount(payment.getPaymentAmount());
        paymentInformationDTO.setBillId(payment.getId());
        paymentInformationDTO.setEmail(email);

        final var remainingBalance = paymentFacade.payBill(paymentInformationDTO);
        final var remainingPaymentResource = new RemainingPaymentResource();
        remainingPaymentResource.setRemainingBalance(remainingBalance);

        return remainingPaymentResource;
    }
}
