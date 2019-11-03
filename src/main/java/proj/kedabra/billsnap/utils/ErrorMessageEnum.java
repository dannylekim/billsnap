package proj.kedabra.billsnap.utils;

public enum ErrorMessageEnum {

    //====================================================================================================
    MULTIPLE_TIP_METHOD("Only one type of tipping is supported. Please make sure only either tip amount or tip percent is set."),
    ACCOUNT_DOES_NOT_EXIST("Account does not exist"),
    LIST_ACCOUNT_DOES_NOT_EXIST("One or more accounts in the list of accounts does not exist: [%s, %s]"),
    LIST_CANNOT_CONTAIN_BILL_CREATOR("List of emails cannot contain bill creator email"),
    NO_USER_FOUND_WITH_EMAIL("No user found with email: %s"),
    EMAIL_ALREADY_EXISTS("This email already exists in the database.");




    private final String message;

    public String getMessage(final Object... parameters) {
        return String.format(this.message, parameters);
    }

    ErrorMessageEnum(final String errorMessage) {
        this.message = errorMessage;
    }

    /**
     * Return if value is equal to the enum chosen
     *
     * @param errorMessage error message to check against
     * @return true if the error message is equivalent to the object desired, false if otherwise
     */
    public boolean is(String errorMessage) {
        return this.message.equals(errorMessage);
    }


}
