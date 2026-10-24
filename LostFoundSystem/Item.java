import java.io.Serializable;
import java.time.LocalDate;

abstract class Item implements Serializable, Searchable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final User reporter;
    private final String name;
    private final Category category;
    private final String description;
    private final Location location;
    private final LocalDate date;
    private final LocalDate reportedOn = LocalDate.now();
    private ItemStatus status = ItemStatus.OPEN;

    protected Item(int id, User reporter, String name, Category category,
                   String description, Location location, LocalDate date) {
        this.id = id;
        this.reporter = reporter;
        this.name = name;
        this.category = category;
        this.description = description;
        this.location = location;
        this.date = date;
    }

    public int getId() { return id; }
    public User getReporter() { return reporter; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }
    public Location getLocation() { return location; }
    public LocalDate getDate() { return date; }
    public LocalDate getReportedOn() { return reportedOn; }
    public ItemStatus getStatus() { return status; }
    void setStatus(ItemStatus status) { this.status = status; }

    // Behaviour that differs between lost and found reports
    public abstract String getType();
    public abstract String getDateLabel();
    public abstract String getExtraInfo();
    public abstract String getSuggestedAction();
    public abstract boolean isClaimable();

    @Override
    public boolean matches(SearchFilter f) {
        if (f.getType() != null && !getType().equalsIgnoreCase(f.getType())) return false;
        if (f.getCategory() != null && category != f.getCategory()) return false;
        if (f.getDistrict() != null && location.getDistrict() != f.getDistrict()) return false;
        if (f.getStatus() != null && status != f.getStatus()) return false;
        if (f.getFrom() != null && date.isBefore(f.getFrom())) return false;
        if (f.getTo() != null && date.isAfter(f.getTo())) return false;
        String k = f.getKeyword();
        if (k != null && !k.trim().isEmpty()) {
            String hay = (name + " " + description + " " + location.getPlace() + " " + getExtraInfo()).toLowerCase();
            for (String word : k.toLowerCase().trim().split("\\s+")) {
                if (!hay.contains(word)) return false;
            }
        }
        return true;
    }

    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append(getType().toUpperCase()).append(" ITEM REPORT #").append(id).append('\n');
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-14s: %s%n", "Item", name));
        sb.append(String.format("%-14s: %s%n", "Category", category));
        sb.append(String.format("%-14s: %s%n", "Description", description));
        sb.append(String.format("%-14s: %s%n", "District", location.getDistrict()));
        sb.append(String.format("%-14s: %s%n", "Specific place", location.getPlace()));
        sb.append(String.format("%-14s: %s%n", getDateLabel(), date));
        sb.append(getExtraInfo()).append('\n');
        sb.append(String.format("%-14s: %s%n", "Status", status));
        sb.append(String.format("%-14s: %s (on %s)%n", "Reported by", reporter.getFullName(), reportedOn));
        sb.append("\nWhat to do: ").append(getSuggestedAction());
        return sb.toString();
    }

    @Override public String toString() { return "#" + id + " [" + getType() + "] " + name; }
}
