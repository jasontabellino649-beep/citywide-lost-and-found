import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class BrowsePanel extends JPanel implements Refreshable {
    private final LostFoundService svc;
    private final User user;
    private final MainFrame frame;

    private final JTextField keyword = new JTextField(14);
    private final JComboBox<String> type = new JComboBox<>(new String[]{"All", "Lost", "Found"});
    private final JComboBox<Object> category = new JComboBox<>();
    private final JComboBox<Object> district = new JComboBox<>();
    private final JComboBox<Object> status = new JComboBox<>();
    private final JTextField from = new JTextField(9);
    private final JTextField to = new JTextField(9);

    private final DefaultTableModel model =
            Ui.model("ID", "Type", "Item", "Category", "District", "Place", "Date", "Status");
    private final JTable table = Ui.table(model);
    private List<Item> shown = new ArrayList<>();
    private SearchFilter current = new SearchFilter();

    BrowsePanel(LostFoundService svc, User user, MainFrame frame) {
        this.svc = svc;
        this.user = user;
        this.frame = frame;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        category.addItem("All categories");
        for (Category c : Category.values()) category.addItem(c);
        district.addItem("All districts");
        for (District d : District.values()) district.addItem(d);
        status.addItem("Any status");
        for (ItemStatus s : ItemStatus.values()) status.addItem(s);
        from.setToolTipText("yyyy-MM-dd");
        to.setToolTipText("yyyy-MM-dd");

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Keyword:"));  row1.add(keyword);
        row1.add(new JLabel("Type:"));     row1.add(type);
        row1.add(new JLabel("Category:")); row1.add(category);
        row1.add(new JLabel("District:")); row1.add(district);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Status:"));   row2.add(status);
        row2.add(new JLabel("Date from (yyyy-MM-dd):")); row2.add(from);
        row2.add(new JLabel("to:"));       row2.add(to);
        JButton search = new JButton("Search");
        JButton reset = new JButton("Reset");
        search.addActionListener(e -> applyFilters());
        reset.addActionListener(e -> resetFilters());
        keyword.addActionListener(e -> applyFilters());
        row2.add(search);
        row2.add(reset);

        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel filters = new JPanel();
        filters.setLayout(new BoxLayout(filters, BoxLayout.Y_AXIS));
        filters.setBorder(BorderFactory.createTitledBorder("Search & filters (all districts of the city)"));
        filters.add(row1);
        filters.add(row2);

        // tint rows: lost = light red, found = light green
        DefaultTableCellRenderer tint = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                if (!sel) {
                    String kind = String.valueOf(t.getValueAt(row, 1));
                    c.setBackground("Lost".equals(kind) ? new Color(255, 241, 241) : new Color(237, 250, 241));
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, tint);
        table.setDefaultRenderer(Number.class, tint);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton details = new JButton("View Details");
        JButton matches = new JButton("Possible Matches");
        JButton contact = new JButton("Contact Reporter");
        JButton claim = new JButton("Claim This Item");
        details.addActionListener(e -> viewDetails());
        matches.addActionListener(e -> showMatches());
        contact.addActionListener(e -> contactReporter());
        claim.addActionListener(e -> claimItem());
        buttons.add(details);
        buttons.add(matches);
        buttons.add(contact);
        buttons.add(claim);
        if (user.canModerate()) {
            JButton setStatus = new JButton("Update Status (Admin)");
            JButton delete = new JButton("Delete Report (Admin)");
            setStatus.addActionListener(e -> updateStatus());
            delete.addActionListener(e -> deleteItem());
            buttons.add(setStatus);
            buttons.add(delete);
        }

        add(filters, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        refresh();
    }

    @Override public void refresh() { fill(svc.search(current)); }

    private void fill(List<Item> list) {
        shown = list;
        model.setRowCount(0);
        for (Item i : list) {
            model.addRow(new Object[]{i.getId(), i.getType(), i.getName(), i.getCategory(),
                    i.getLocation().getDistrict(), i.getLocation().getPlace(), i.getDate(), i.getStatus()});
        }
    }

    private void applyFilters() {
        try {
            SearchFilter f = new SearchFilter();
            f.setKeyword(keyword.getText().trim());
            String t = (String) type.getSelectedItem();
            f.setType("All".equals(t) ? null : t);
            Object c = category.getSelectedItem();
            f.setCategory(c instanceof Category ? (Category) c : null);
            Object d = district.getSelectedItem();
            f.setDistrict(d instanceof District ? (District) d : null);
            Object s = status.getSelectedItem();
            f.setStatus(s instanceof ItemStatus ? (ItemStatus) s : null);
            if (!from.getText().trim().isEmpty()) f.setFrom(Validator.parseDate("Date from", from.getText()));
            if (!to.getText().trim().isEmpty()) f.setTo(Validator.parseDate("Date to", to.getText()));
            if (f.getFrom() != null && f.getTo() != null && f.getFrom().isAfter(f.getTo())) {
                throw new ValidationException("'Date from' must not be after 'Date to'.");
            }
            current = f;
            refresh();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void resetFilters() {
        keyword.setText("");
        type.setSelectedIndex(0);
        category.setSelectedIndex(0);
        district.setSelectedIndex(0);
        status.setSelectedIndex(0);
        from.setText("");
        to.setText("");
        current = new SearchFilter();
        refresh();
    }

    private Item selected() {
        int row = Ui.selectedRow(table);
        if (row < 0 || row >= shown.size()) {
            Ui.error(this, "Please select an item from the table first.");
            return null;
        }
        return shown.get(row);
    }

    private void viewDetails() {
        Item item = selected();
        if (item != null) Ui.showText(this, "Item #" + item.getId(), item.getDetails());
    }

    private void showMatches() {
        Item item = selected();
        if (item != null) Ui.showMatches(this, item, svc.findMatches(item));
    }

    private void contactReporter() {
        Item item = selected();
        if (item == null) return;
        String text = Ui.askText(this, "Contact reporter of " + item,
                "Write your message to " + item.getReporter().getFullName() + ":");
        if (text == null) return;
        try {
            svc.sendMessage(user, item.getReporter(), item, text);
            Ui.info(this, "Message sent. The reporter will see it in their Inbox.");
            frame.dataChanged();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void claimItem() {
        Item item = selected();
        if (item == null) return;
        if (!item.isClaimable()) {
            Ui.error(this, "Only open found-item reports can be claimed.\n"
                    + "If this is a lost-item report and you found it, use 'Contact Reporter'.");
            return;
        }
        String proof = Ui.askText(this, "Claim item #" + item.getId(),
                "Describe your proof of ownership (unique marks, contents, serial numbers):");
        if (proof == null) return;
        try {
            svc.submitClaim(user, item, proof);
            Ui.info(this, "Claim submitted! An administrator will review it.\nTrack it in 'My Activity' and your Inbox.");
            frame.dataChanged();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void updateStatus() {
        Item item = selected();
        if (item == null) return;
        Object choice = JOptionPane.showInputDialog(this, "New status for item #" + item.getId(),
                "Update status", JOptionPane.QUESTION_MESSAGE, null, ItemStatus.values(), item.getStatus());
        if (choice == null) return;
        try {
            svc.setStatus(user, item, (ItemStatus) choice);
            frame.dataChanged();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void deleteItem() {
        Item item = selected();
        if (item == null) return;
        int r = JOptionPane.showConfirmDialog(this,
                "Delete " + item + " and its related claims and messages?\nThis cannot be undone.",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        try {
            svc.deleteItem(user, item);
            frame.dataChanged();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}
