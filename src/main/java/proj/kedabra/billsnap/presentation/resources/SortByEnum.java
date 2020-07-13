package proj.kedabra.billsnap.presentation.resources;

public enum SortByEnum {
    START_DATE("START_DATE"),
    END_DATE("END_DATE"),
    STATUS("STATUS"),
    CATEGORY("CATEGORY");

    private final String status;

    SortByEnum(final String status) {

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
