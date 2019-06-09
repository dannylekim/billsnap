package proj.kedabra.billsnap.utils.enums;

public enum AccountStatusEnum {
    UNREGISTERED("UNREGISTERED"),
    REGISTERED("REGISTERED");

    private final String status;

    AccountStatusEnum(final String status) {
        this.status = status;
    }

    /**
     * Return if value is equal to the enum chosen
     *
     * @param status status to check against
     * @return true if the status is equivalent to the object desired, false if otherwise
     */
    public boolean is(String status) {
        return this.status.equals(status);
    }

}
