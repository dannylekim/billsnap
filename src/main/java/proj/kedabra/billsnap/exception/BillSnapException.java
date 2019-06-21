package proj.kedabra.billsnap.exception;

import lombok.Getter;

@Getter
abstract class BillSnapException extends RuntimeException {

    private static final long serialVersionUID = 3872812859698299907L;

    private final String message;

    private final Throwable ex;

    BillSnapException(final String message, final Throwable ex) {
        this.message = message;
        this.ex = ex;
    }

    BillSnapException(final String message) {
        this.message = message;
        this.ex = null;
    }
}
