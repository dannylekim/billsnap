package proj.kedabra.billsnap.presentation.controllers;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import proj.kedabra.billsnap.business.dto.PaymentInformationDTO;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.facade.PaymentFacade;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;
import proj.kedabra.billsnap.presentation.resources.PaymentResource;
import proj.kedabra.billsnap.utils.CacheNames;

@RestController
@RequestMapping("/resolve/bills")
public class ResolveBillController {

    private final PaymentFacade paymentFacade;

    private final BillFacade billFacade;

    private final BillMapper mapper;

    public ResolveBillController(final PaymentFacade paymentFacade, final BillFacade billFacade, final BillMapper mapper) {
        this.paymentFacade = paymentFacade;
        this.billFacade = billFacade;
        this.mapper = mapper;
    }

    @CacheEvict(value = CacheNames.PAYMENTS, key = "#principal.name")
    @PostMapping
    @Operation(summary = "Add a bill", description = "Pay a personal bill.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "You've successfully paid a bill!")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Cannot pay a bill with wrong inputs.")
    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are unauthorized to access this resource.")
    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#payment.id)")
    public BillSplitResource payBill(@RequestBody @Valid @Parameter(required = true, name = "Payment details", description = "Amount paid to bill") final PaymentResource payment,
                                     final BindingResult bindingResult,
                                     @AuthenticationPrincipal Principal principal) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }

        final var email = principal.getName();
        final var paymentInformationDTO = new PaymentInformationDTO();
        paymentInformationDTO.setAmount(payment.getPaymentAmount());
        paymentInformationDTO.setBillId(payment.getId());
        paymentInformationDTO.setEmail(email);

        paymentFacade.payBill(paymentInformationDTO);

        return mapper.toResource(billFacade.getDetailedBill(payment.getId()));
    }
}
