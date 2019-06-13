package proj.kedabra.billsnap.utils.enums;

public enum GroupRoleEnum {
    ADMIN("ADMIN"),
    MEMBER("MEMBER");

    private final String groupRole;

    GroupRoleEnum(final String groupRole) {
        this.groupRole = groupRole;
    }

    /**
     * Return if value is equal to the enum chosen
     *
     * @param groupRole groupRole to check against
     * @return true if the groupRole is equivalent to the object desired, false if otherwise
     */
    public boolean is(String groupRole) {
        return this.groupRole.equals(groupRole);
    }
}
