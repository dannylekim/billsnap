package proj.kedabra.billsnap.presentation.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import proj.kedabra.billsnap.business.dto.PaymentsOwedDTO;
import proj.kedabra.billsnap.business.facade.PaymentsFacade;
import proj.kedabra.billsnap.business.mapper.PaymentMapper;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.PaymentsOwedRessource;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;

@RestController
public class PaymentController {

    private PaymentsFacade paymentsFacade;

    private PaymentMapper paymentMapper;

    public PaymentController(final PaymentsFacade paymentsFacade, final PaymentMapper paymentMapper) {
        this.paymentsFacade = paymentsFacade;
        this.paymentMapper = paymentMapper;
    }

    @GetMapping("payments")
    @ApiOperation(value = "Get amounts", notes = "Get all amounts owed by account")
    @ApiResponses({
            @ApiResponse(code = 200, response = PaymentsOwedRessource.class, message = "Successfully retrieved all amounts owed!"),
            @ApiResponse(code = 401, response = ApiError.class, message = "You are unauthorized to access this resource."),
            @ApiResponse(code = 403, response = ApiError.class, message = "You are forbidden to access this resource."),
    })
    @ResponseStatus(HttpStatus.OK)
    public PaymentsOwedRessource getAllAmountsOwed(@ApiIgnore @AuthenticationPrincipal final Principal principal) {
        final PaymentsOwedDTO paymentsOwedDTO = paymentsFacade.getAmountsOwed(principal.getName());
        return paymentMapper.toRessource(paymentsOwedDTO);
    }
}
