package proj.kedabra.billsnap.business.exception;

import lombok.Getter;

@Getter
public class AccessForbiddenException extends BillSnapException {

    private static final long serialVersionUID = 8230724343207381403L;

    public AccessForbiddenException(final String message) {
        super(message);
    }
}
