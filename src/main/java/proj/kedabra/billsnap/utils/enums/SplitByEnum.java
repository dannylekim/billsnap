package proj.kedabra.billsnap.utils.enums;

public enum SplitByEnum {
    BALANCE("BALANCE"),
    ITEM("ITEM");

    private final String splitByType;

    SplitByEnum(final String splitByType) {
        this.splitByType = splitByType;
    }

    /**
     * Return if value is equal to the enum chosen
     *
     * @param splitByType splitByType to check against
     * @return true if the splitByType is equivalent to the object desired, false if otherwise
     */
    public boolean is(String splitByType) {
        return this.splitByType.equals(splitByType);
    }

}
