import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class ClaimsReviewPanel extends JPanel implements Refreshable {
    private final LostFoundService svc;
    private final User user;
    private final MainFrame frame;
    private final DefaultTableModel model =
            Ui.model("Claim #", "Item", "Found at", "Claimant", "Contact", "Submitted", "Status", "Proof (preview)");
    private final JTable table = Ui.table(model);
    private List<Claim> shown = new ArrayList<>();

    ClaimsReviewPanel(LostFoundService svc, User user, MainFrame frame) {
        this.svc = svc;
        this.user = user;
        this.frame = frame;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton view = new JButton("View Full Claim");
        JButton approve = new JButton("Approve Claim");
        JButton reject = new JButton("Reject Claim");
        view.addActionListener(e -> viewClaim());
        approve.addActionListener(e -> review(true));
        reject.addActionListener(e -> review(false));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.add(view);
        buttons.add(approve);
        buttons.add(reject);

        add(new JLabel("<html><b>Review ownership claims.</b> Approving a claim marks the item as Claimed / Returned "
                + "and notifies the claimant and the finder.</html>"), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        refresh();
    }

    @Override
    public void refresh() {
        shown = svc.getClaims();
        model.setRowCount(0);
        for (Claim c : shown) {
            String proof = c.getProof().length() > 40 ? c.getProof().substring(0, 40) + "..." : c.getProof();
            model.addRow(new Object[]{c.getId(), c.getItem().toString(), c.getItem().getLocation().getDistrict(),
                    c.getClaimant().getFullName(), c.getClaimant().getContact(),
                    c.getSubmittedOn(), c.getStatus(), proof});
        }
    }

    private Claim selected() {
        int row = Ui.selectedRow(table);
        if (row < 0 || row >= shown.size()) {
            Ui.error(this, "Please select a claim first.");
            return null;
        }
        return shown.get(row);
    }

    private void viewClaim() {
        Claim c = selected();
        if (c == null) return;
        Ui.showText(this, "Claim #" + c.getId(),
                "CLAIM #" + c.getId() + "  (" + c.getStatus() + ")\n"
                        + "----------------------------------------\n"
                        + "Item     : " + c.getItem() + "\n"
                        + "Claimant : " + c.getClaimant().getFullName() + " - " + c.getClaimant().getContact() + "\n"
                        + "Submitted: " + c.getSubmittedOn() + "\n\n"
                        + "PROOF OF OWNERSHIP\n" + c.getProof() + "\n\n"
                        + "ITEM DETAILS\n" + c.getItem().getDetails());
    }

    private void review(boolean approve) {
        Claim c = selected();
        if (c == null) return;
        String note = Ui.askText(this, approve ? "Approve claim" : "Reject claim",
                "Optional note for the claimant (pickup instructions or reason):");
        if (note == null) return;
        try {
            svc.reviewClaim(user, c, approve, note);
            Ui.info(this, "Claim " + (approve ? "approved." : "rejected.") + " The claimant has been notified.");
            frame.dataChanged();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}
