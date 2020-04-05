package proj.kedabra.billsnap.business.exception;

import lombok.Getter;

@Getter
public class FunctionalWorkflowException extends BillSnapException {

    public FunctionalWorkflowException(final String message) {
        super(message);
    }
}
