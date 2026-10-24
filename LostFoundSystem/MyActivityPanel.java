import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class MyActivityPanel extends JPanel implements Refreshable {
    private final LostFoundService svc;
    private final User user;
    private final MainFrame frame;
    private final DefaultTableModel reportModel = Ui.model("ID", "Type", "Item", "District", "Date", "Status");
    private final DefaultTableModel claimModel =
            Ui.model("Claim #", "Item", "Item status", "Submitted", "Claim status", "Admin note");
    private final JTable reportTable = Ui.table(reportModel);
    private final JTable claimTable = Ui.table(claimModel);
    private List<Item> shown = new ArrayList<>();

    MyActivityPanel(LostFoundService svc, User user, MainFrame frame) {
        this.svc = svc;
        this.user = user;
        this.frame = frame;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton matches = new JButton("Possible Matches");
        JButton close = new JButton("Close Report");
        matches.addActionListener(e -> showMatches());
        close.addActionListener(e -> closeReport());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.add(matches);
        buttons.add(close);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("My reports (status tracking)"));
        top.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createTitledBorder("My claims"));
        bottom.add(new JScrollPane(claimTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        shown = svc.itemsBy(user);
        reportModel.setRowCount(0);
        for (Item i : shown) {
            reportModel.addRow(new Object[]{i.getId(), i.getType(), i.getName(),
                    i.getLocation().getDistrict(), i.getDate(), i.getStatus()});
        }
        claimModel.setRowCount(0);
        for (Claim c : svc.claimsBy(user)) {
            claimModel.addRow(new Object[]{c.getId(), c.getItem().getName(), c.getItem().getStatus(),
                    c.getSubmittedOn(), c.getStatus(), c.getAdminNote()});
        }
    }

    private Item selected() {
        int row = Ui.selectedRow(reportTable);
        if (row < 0 || row >= shown.size()) {
            Ui.error(this, "Please select one of your reports first.");
            return null;
        }
        return shown.get(row);
    }

    private void showMatches() {
        Item item = selected();
        if (item != null) Ui.showMatches(this, item, svc.findMatches(item));
    }

    private void closeReport() {
        Item item = selected();
        if (item == null) return;
        int r = JOptionPane.showConfirmDialog(this, "Close report " + item + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;
        try {
            svc.closeReport(user, item);
            frame.dataChanged();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}
