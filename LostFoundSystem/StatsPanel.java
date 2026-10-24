import javax.swing.*;
import java.awt.*;
import java.util.List;

class StatsPanel extends JPanel implements Refreshable {
    private final LostFoundService svc;
    private final JTextArea out = new JTextArea();

    StatsPanel(LostFoundService svc) {
        this.svc = svc;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        out.setEditable(false);
        out.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(out), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        List<Item> items = svc.getItems();
        long lost = items.stream().filter(i -> i.getType().equals("Lost")).count();
        long found = items.stream().filter(i -> i.getType().equals("Found")).count();
        long returned = items.stream()
                .filter(i -> i.getType().equals("Found") && i.getStatus() == ItemStatus.CLAIMED).count();
        long pending = svc.getClaims().stream().filter(c -> c.getStatus() == ClaimStatus.PENDING).count();

        StringBuilder sb = new StringBuilder();
        sb.append("CITYWIDE LOST & FOUND - SUMMARY\n");
        sb.append("========================================\n");
        sb.append(String.format("Total reports        : %d%n", items.size()));
        sb.append(String.format("Lost reports         : %d%n", lost));
        sb.append(String.format("Found reports        : %d%n", found));
        sb.append(String.format("Found items returned : %d  (recovery rate %.0f%%)%n",
                returned, found == 0 ? 0.0 : 100.0 * returned / found));
        sb.append(String.format("Claims awaiting review: %d%n", pending));
        sb.append(String.format("Registered users     : %d%n", svc.getUsers().size()));

        sb.append("\nBY STATUS\n");
        for (ItemStatus s : ItemStatus.values()) {
            bar(sb, s.toString(), items.stream().filter(i -> i.getStatus() == s).count(), items.size());
        }
        sb.append("\nBY CATEGORY\n");
        for (Category c : Category.values()) {
            bar(sb, c.toString(), items.stream().filter(i -> i.getCategory() == c).count(), items.size());
        }
        sb.append("\nBY DISTRICT\n");
        for (District d : District.values()) {
            bar(sb, d.toString(), items.stream().filter(i -> i.getLocation().getDistrict() == d).count(), items.size());
        }
        out.setText(sb.toString());
        out.setCaretPosition(0);
    }

    private static void bar(StringBuilder sb, String label, long n, long total) {
        sb.append(String.format("  %-26s %3d  ", label, n));
        int len = total == 0 ? 0 : (int) Math.round(30.0 * n / total);
        for (int i = 0; i < len; i++) sb.append('#');
        sb.append('\n');
    }
}
