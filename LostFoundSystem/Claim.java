import java.io.Serializable;
import java.time.LocalDate;

class Claim implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final Item item;
    private final User claimant;
    private final String proof;
    private final LocalDate submittedOn = LocalDate.now();
    private ClaimStatus status = ClaimStatus.PENDING;
    private String adminNote = "";

    Claim(int id, Item item, User claimant, String proof) {
        this.id = id;
        this.item = item;
        this.claimant = claimant;
        this.proof = proof;
    }

    public int getId() { return id; }
    public Item getItem() { return item; }
    public User getClaimant() { return claimant; }
    public String getProof() { return proof; }
    public LocalDate getSubmittedOn() { return submittedOn; }
    public ClaimStatus getStatus() { return status; }
    public String getAdminNote() { return adminNote; }

    void review(boolean approve, String note) {
        this.status = approve ? ClaimStatus.APPROVED : ClaimStatus.REJECTED;
        this.adminNote = note == null ? "" : note.trim();
    }
}
