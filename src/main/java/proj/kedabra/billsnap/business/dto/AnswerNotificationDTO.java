package proj.kedabra.billsnap.business.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AnswerNotificationDTO implements Serializable {

    private Long invitationId;

    private boolean answer;

    private String email;

}
