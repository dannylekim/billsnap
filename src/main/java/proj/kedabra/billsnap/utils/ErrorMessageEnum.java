package proj.kedabra.billsnap.utils;

public enum ErrorMessageEnum {

    //====================================================================================================
    MULTIPLE_TIP_METHOD("Only one type of tipping is supported. Please make sure only either tip amount or tip percent is set."),
    ACCOUNT_DOES_NOT_EXIST("Account does not exist"),
    LIST_ACCOUNT_DOES_NOT_EXIST("One or more accounts in the list of accounts does not exist: {}"),
    LIST_CANNOT_CONTAIN_BILL_CREATOR("List of emails cannot contain bill creator email"),
    NO_USER_FOUND_WITH_EMAIL("No user found with email '{}'"),
    EMAIL_ALREADY_EXISTS("This email already exists in the database."),
    INCORRECT_LOGIN_METHOD("Incorrect login request method."),
    MEDIA_TYPE_NOT_JSON("Login request input is not JSON content-type."),
    INVALID_LOGIN_INPUTS("Invalid Login Inputs. Please fix the following errors"),
    UNAUTHORIZED_ACCESS("Access is unauthorized!"),
    WRONG_REQ_METHOD("Incorrect login request method."),
    BAD_CREDENTIALS("Username or password is incorrect."),
    INTERNAL_SERVER_ERROR("Server error has occurred, please try again later."),
    CANNOT_PAY_MORE_THAN_OWED("Cannot pay more than the amount owed."),
    ACCOUNT_IS_NOT_ASSOCIATED_TO_BILL("Account does not have the bill specified."),
    BILL_ALREADY_RESOLVED("Bill is already resolved. Cannot perform action."),
    BILL_ALREADY_PAID_FOR("The user has already paid their bill."),
    BILL_ID_DOES_NOT_EXIST("No bill exists with id: {}"),
    ITEM_ID_DOES_NOT_EXIST("No item exists with id: {}"),
    SOME_ACCOUNTS_NONEXISTENT_IN_BILL("Not all accounts are in the bill: {}"),
    SOME_ITEMS_NONEXISTENT_IN_BILL("Not all items exist in the bill: {}"),
    LIST_ACCOUNT_ALREADY_IN_BILL("One or more accounts is already invited or part of the bill: {}"),
    USER_IS_NOT_BILL_RESPONSIBLE("The user making the request is not the Bill responsible"),
    GIVEN_VALUES_NOT_INTEGER_VALUED("The given percentages are not integer valued: {}"),
    DUPLICATE_EMAILS_IN_ASSOCIATE_USERS("There are duplicate emails being called: {}"),
    LIST_ACCOUNT_DECLINED("One or more accounts trying to be associated with an item has already declined the invitation: {}"),
    NOTIFICATION_ID_DOES_NOT_EXIST("No notification exists with id: {}"),
    ACCOUNT_NOT_ASSOCIATED_TO_NOTIFICATION("Account is not associated to the specified notification."),
    WRONG_BILL_STATUS("The bill is not in {} status."),
    WRONG_TIP_FORMAT("Tip format is incorrect."),

    //=========================================TESTING ONLY===============================================
    TEST_DIFFERENT_POSITION_PARAMS("First Param: {} , Second Param: {} , Third Param: {}");


    private final String message;

    public String getMessage(final String... parameters) {

        String resolvedMessage = this.message;

        for (String param : parameters) {
            resolvedMessage = resolvedMessage.replaceFirst("\\{}", param);
        }

        return resolvedMessage;
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
