import java.time.LocalDate;

class LostItem extends Item {
    private static final long serialVersionUID = 1L;
    private final String reward;

    LostItem(int id, User reporter, String name, Category category, String description,
             Location location, LocalDate date, String reward) {
        super(id, reporter, name, category, description, location, date);
        this.reward = reward;
    }

    @Override public String getType() { return "Lost"; }
    @Override public String getDateLabel() { return "Date lost"; }
    @Override public String getExtraInfo() {
        return String.format("%-14s: %s", "Reward", reward.isEmpty() ? "none offered" : reward);
    }
    @Override public String getSuggestedAction() {
        return "Found this item? Select it and press 'Contact Reporter' to reach the owner.";
    }
    @Override public boolean isClaimable() { return false; }
}
