package proj.kedabra.billsnap.presentation.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.facade.PaymentFacade;
import proj.kedabra.billsnap.business.mapper.PaymentMapper;
import proj.kedabra.billsnap.config.SwaggerConfiguration;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.PaymentOwedResource;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;

@RestController
public class PaymentController {

    private PaymentFacade paymentFacade;

    private PaymentMapper paymentMapper;

    public PaymentController(final PaymentFacade paymentFacade, final PaymentMapper paymentMapper) {
        this.paymentFacade = paymentFacade;
        this.paymentMapper = paymentMapper;
    }

    @GetMapping("/payments")
    @ApiOperation(value = "Get amounts", notes = "Get all amounts owed by account", authorizations = {@Authorization(value = SwaggerConfiguration.API_KEY)})
    @ApiResponses({
            @ApiResponse(code = 200, response = PaymentOwedResource.class, message = "Successfully retrieved all amounts owed!"),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.OK)
    public PaymentOwedResource getAllAmountsOwed(@ApiIgnore @AuthenticationPrincipal final Principal principal) {
        final PaymentOwedDTO paymentOwedDTO = paymentFacade.getAmountsOwed(principal.getName());
        return paymentMapper.toResource(paymentOwedDTO);
    }
}
