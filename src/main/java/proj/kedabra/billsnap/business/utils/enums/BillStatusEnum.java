package proj.kedabra.billsnap.business.utils.enums;

public enum BillStatusEnum {
    RESOLVED("RESOLVED"),
    OPEN("OPEN"),
    IN_PROGRESS("IN_PROGRESS");

    private final String status;

    BillStatusEnum(final String status) {

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
