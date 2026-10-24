import java.time.LocalDate;

class SearchFilter {
    private String keyword;
    private String type;          // "Lost", "Found" or null for all
    private Category category;
    private District district;
    private ItemStatus status;
    private LocalDate from;
    private LocalDate to;

    public String getKeyword() { return keyword; }
    public String getType() { return type; }
    public Category getCategory() { return category; }
    public District getDistrict() { return district; }
    public ItemStatus getStatus() { return status; }
    public LocalDate getFrom() { return from; }
    public LocalDate getTo() { return to; }

    public void setKeyword(String keyword) { this.keyword = keyword; }
    public void setType(String type) { this.type = type; }
    public void setCategory(Category category) { this.category = category; }
    public void setDistrict(District district) { this.district = district; }
    public void setStatus(ItemStatus status) { this.status = status; }
    public void setFrom(LocalDate from) { this.from = from; }
    public void setTo(LocalDate to) { this.to = to; }
}
