class RegularUser extends User {
    private static final long serialVersionUID = 1L;
    RegularUser(int id, String username, String password, String fullName, String contact) {
        super(id, username, password, fullName, contact);
    }
    @Override public String getRole() { return "Citizen"; }
    @Override public boolean canModerate() { return false; }
}
