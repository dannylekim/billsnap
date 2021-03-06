package proj.kedabra.billsnap.fixtures;

import org.springframework.validation.FieldError;

public final class FieldErrorFixture {

    private FieldErrorFixture() {}

    public static FieldError getDefault(int uniqueIdentifier) {
        final var objectName = "objectName" + uniqueIdentifier;
        final var field = "field" + uniqueIdentifier;
        final var rejectedValue = "rejectedValue" + uniqueIdentifier;
        final var defaultMessage = "defaultMessage" + uniqueIdentifier;
        final var code = new String[]{"code" + uniqueIdentifier, "real" + uniqueIdentifier};
        final var arguments = new String[]{"one" + uniqueIdentifier, "two" + uniqueIdentifier, "three" + uniqueIdentifier};

        return new FieldError(objectName, field, rejectedValue, false, code, arguments, defaultMessage);
    }
}
