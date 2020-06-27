package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.business.dto.AnswerNotificationDTO;

public final class AnswerNotificationDTOFixture {

    private AnswerNotificationDTOFixture() {
    }

    public static AnswerNotificationDTO getDefault() {
        final AnswerNotificationDTO dto = new AnswerNotificationDTO();
        dto.setInvitationId(1235L);
        dto.setAnswer(true);
        dto.setEmail("hellomotto@email.com");
        return dto;
    }
}
