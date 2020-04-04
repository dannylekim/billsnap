package proj.kedabra.billsnap.business.exception;

import lombok.Getter;

@Getter
public class MethodNotAllowedException extends BillSnapException {

    private static final long serialVersionUID = -5622747147527623525L;

    public MethodNotAllowedException(final String message) {
        super(message);
    }
}
