package proj.kedabra.billsnap.business.facade.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AnswerNotificationDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.facade.NotificationFacade;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Notifications;
import proj.kedabra.billsnap.business.service.BillService;
import proj.kedabra.billsnap.business.service.NotificationService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@Service
public class NotificationFacadeImpl implements NotificationFacade {

    private final NotificationService notificationService;

    private final BillService billService;

    private final BillFacade billFacade;

    public NotificationFacadeImpl(final NotificationService notificationService, final BillService billService, final BillFacade billFacade) {
        this.notificationService = notificationService;
        this.billService = billService;
        this.billFacade = billFacade;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillSplitDTO answerInvitation(AnswerNotificationDTO answerNotificationDTO) {
        final Notifications notification = notificationService.getNotification(answerNotificationDTO.getInvitationId());
        billService.verifyBillStatus(notification.getBill(), BillStatusEnum.OPEN);

        final Bill bill = notificationService.answerInvitation(answerNotificationDTO.getInvitationId(), answerNotificationDTO.isAnswer());

        return billFacade.getBillSplitDTO(bill);
    }

}
