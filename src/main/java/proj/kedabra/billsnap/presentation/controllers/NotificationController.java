package proj.kedabra.billsnap.presentation.controllers;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import proj.kedabra.billsnap.business.dto.AnswerNotificationDTO;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.business.facade.NotificationFacade;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.resources.AnswerNotificationResource;
import proj.kedabra.billsnap.presentation.resources.BillSplitResource;
import proj.kedabra.billsnap.utils.CacheNames;

@RestController
public class NotificationController {

    private final NotificationFacade notificationFacade;

    private final BillMapper billMapper;

    @Autowired
    public NotificationController(final NotificationFacade notificationFacade, final BillMapper billMapper) {
        this.notificationFacade = notificationFacade;
        this.billMapper = billMapper;
    }

    @CachePut(value = CacheNames.BILLS, key = "#billId")
    @PostMapping("/invitations/{billId}")
    @Operation(summary = "Answer bill invitation", description = "Answer an invitation to join a bill")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BillSplitResource.class)), description = "Successfully answered invitation.")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "There is an error with the input parameters.")
    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Access is unauthorized!")
    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "You are forbidden to access this resource.")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "The invitation does not exist.")
    @ApiResponse(responseCode = "405", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "The bill is not in Open status.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('INVITATION_' + #billId)")
    public BillSplitResource answerInvitation(@Parameter(required = true, name = "billId", description = "Bill ID")
                                              @PathVariable("billId") final Long billId,
                                              @Parameter(required = true, name = "Answer to bill invitation", description = "Answer to bill invitation")
                                              @RequestBody @Valid final AnswerNotificationResource answerInvitationResource,
                                              final BindingResult bindingResult,
                                              @AuthenticationPrincipal final Principal principal) {
        verifyBindingResult(bindingResult);

        final boolean answer = answerInvitationResource.getAnswer();
        final var billSplitDTO = notificationFacade.answerInvitation(new AnswerNotificationDTO(billId, answer, principal.getName()));

        if (answer) {
            return billMapper.toResource(billSplitDTO);
        } else {
            return null;
        }
    }

    private void verifyBindingResult(final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException(bindingResult.getAllErrors());
        }
    }

}
