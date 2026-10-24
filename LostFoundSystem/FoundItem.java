import java.time.LocalDate;

class FoundItem extends Item {
    private static final long serialVersionUID = 1L;
    private final String keptAt;

    FoundItem(int id, User reporter, String name, Category category, String description,
              Location location, LocalDate date, String keptAt) {
        super(id, reporter, name, category, description, location, date);
        this.keptAt = keptAt;
    }

    @Override public String getType() { return "Found"; }
    @Override public String getDateLabel() { return "Date found"; }
    @Override public String getExtraInfo() { return String.format("%-14s: %s", "Kept at", keptAt); }
    @Override public String getSuggestedAction() {
        return "Is this yours? Press 'Claim This Item' and describe proof of ownership.";
    }
    @Override public boolean isClaimable() {
        return getStatus() == ItemStatus.OPEN || getStatus() == ItemStatus.CLAIM_PENDING;
    }
}
