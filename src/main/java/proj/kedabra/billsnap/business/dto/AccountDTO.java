package proj.kedabra.billsnap.business.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class AccountDTO extends BaseAccountDTO implements Serializable {

    private Long id;

    private String email;

    private String password;

}
