package proj.kedabra.billsnap.business.facade;

import proj.kedabra.billsnap.business.dto.AnswerNotificationDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;

public interface NotificationFacade {

    BillSplitDTO answerInvitation(AnswerNotificationDTO answerNotificationDTO);

}
