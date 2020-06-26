package proj.kedabra.billsnap.business.exception;

import lombok.Getter;

@Getter
@SuppressWarnings("MissingOverride")
abstract class BillSnapException extends RuntimeException {

    private final String message;

    private final Throwable ex;

    BillSnapException(final String message) {
        this.message = message;
        this.ex = null;
    }
}
