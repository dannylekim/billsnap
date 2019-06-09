package proj.kedabra.billsnap.utils.enums;

public enum GenderEnum {
    MALE("MALE"),
    FEMALE("FEMALE"),
    OTHER("OTHER");

    private final String gender;

    GenderEnum(String gender) {
        this.gender = gender;
    }

    /**
     * Return if value is equal to the enum chosen
     *
     * @param gender gender to check against
     * @return true if the gender is equivalent to the object desired, false if otherwise
     */
    public boolean is(String gender) {
        return this.gender.equals(gender);
    }
}
