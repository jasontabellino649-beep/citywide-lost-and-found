enum ItemStatus {
    OPEN("Open"), CLAIM_PENDING("Claim Pending"), CLAIMED("Claimed / Returned"), CLOSED("Closed");

    private final String label;
    ItemStatus(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
