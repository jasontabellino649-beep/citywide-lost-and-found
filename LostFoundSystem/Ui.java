import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

final class Ui {
    static final Color PRIMARY = new Color(31, 78, 121);

    private Ui() { }

    static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    static String askText(Component parent, String title, String prompt) {
        JTextArea area = new JTextArea(5, 32);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        int r = JOptionPane.showConfirmDialog(parent, new Object[]{prompt, new JScrollPane(area)},
                title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return r == JOptionPane.OK_OPTION ? area.getText() : null;
    }

    static void showText(Component parent, String title, String text) {
        JTextArea area = new JTextArea(text, 14, 46);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(parent, new JScrollPane(area), title, JOptionPane.PLAIN_MESSAGE);
    }

    static void showMatches(Component parent, Item item, List<MatchResult> matches) {
        if (matches.isEmpty()) {
            info(parent, "No possible matches yet for " + item + ".\nNew reports are checked automatically.");
            return;
        }
        StringBuilder sb = new StringBuilder("Possible matches for " + item + ":\n\n");
        for (MatchResult m : matches) {
            Item o = m.getItem();
            sb.append(String.format("%3d%% match  #%d [%s] %s%n", m.getScore(), o.getId(), o.getType(), o.getName()));
            sb.append(String.format("             %s | %s%n", o.getLocation(), o.getDate()));
            sb.append(String.format("             Status: %s%n%n", o.getStatus()));
        }
        sb.append("Tip: use Contact Reporter or Claim This Item from the Browse tab.");
        showText(parent, "Possible matches", sb.toString());
    }

    static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                Object v = getRowCount() > 0 ? getValueAt(0, col) : null;
                return v == null ? Object.class : v.getClass();
            }
        };
    }

    static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(24);
        t.setAutoCreateRowSorter(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    static int selectedRow(JTable t) {
        int r = t.getSelectedRow();
        return r < 0 ? -1 : t.convertRowIndexToModel(r);
    }

    static JPanel padded(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return p;
    }
}
