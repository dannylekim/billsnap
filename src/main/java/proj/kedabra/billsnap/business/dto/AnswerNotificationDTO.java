package proj.kedabra.billsnap.business.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerNotificationDTO implements Serializable {

    private Long billId;

    private boolean answer;

    private String email;

}
