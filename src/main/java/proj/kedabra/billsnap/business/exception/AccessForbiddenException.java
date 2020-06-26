package proj.kedabra.billsnap.business.exception;

import lombok.Getter;

@Getter
public class AccessForbiddenException extends BillSnapException {

    public AccessForbiddenException(final String message) {
        super(message);
    }
}
