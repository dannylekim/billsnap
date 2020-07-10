package proj.kedabra.billsnap.presentation.controllers;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.facade.PaymentFacade;
import proj.kedabra.billsnap.business.mapper.PaymentMapper;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.PaymentOwedResource;
import proj.kedabra.billsnap.utils.CacheNames;

@RestController
public class PaymentController {

    private final PaymentFacade paymentFacade;

    private final PaymentMapper paymentMapper;

    public PaymentController(final PaymentFacade paymentFacade, final PaymentMapper paymentMapper) {
        this.paymentFacade = paymentFacade;
        this.paymentMapper = paymentMapper;
    }

    @Cacheable(value = CacheNames.PAYMENTS, key = "#principal.name")
    @GetMapping("/payments")
    @Operation(summary = "Get amounts", description = "Get all amounts owed by account")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all amounts owed!")
    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource.")
    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource.")
    @ResponseStatus(HttpStatus.OK)
    public List<PaymentOwedResource> getAllAmountsOwed(@AuthenticationPrincipal final Principal principal) {
        final List<PaymentOwedDTO> paymentOwedDTO = paymentFacade.getAmountsOwed(principal.getName());
        return paymentOwedDTO.stream().map(paymentMapper::toResource).collect(Collectors.toList());
    }
}
