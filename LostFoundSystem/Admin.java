class Admin extends User {
    private static final long serialVersionUID = 1L;
    Admin(int id, String username, String password, String fullName, String contact) {
        super(id, username, password, fullName, contact);
    }
    @Override public String getRole() { return "Administrator"; }
    @Override public boolean canModerate() { return true; }
}
