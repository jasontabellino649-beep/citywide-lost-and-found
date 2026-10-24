import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

class ReportFormPanel extends JPanel {
    private final LostFoundService svc;
    private final User user;
    private final MainFrame frame;
    private final boolean lost;

    private final JTextField name = new JTextField(30);
    private final JComboBox<Category> category = new JComboBox<>(Category.values());
    private final JTextArea description = new JTextArea(4, 30);
    private final JComboBox<District> district = new JComboBox<>(District.values());
    private final JTextField place = new JTextField(30);
    private final JTextField date = new JTextField(10);
    private final JTextField extra = new JTextField(30);

    ReportFormPanel(LostFoundService svc, User user, MainFrame frame, boolean lost) {
        this.svc = svc;
        this.user = user;
        this.frame = frame;
        this.lost = lost;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel heading = new JLabel(lost
                ? "<html><h2>Report a Lost Item</h2>Tell the city what you lost and where. Finders can contact you.</html>"
                : "<html><h2>Register a Found Item</h2>Describe what you found so the owner can claim it.</html>");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Item details"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        date.setText(LocalDate.now().format(Validator.DATE_FMT));

        addRow(form, 0, "Item name *", name);
        addRow(form, 1, "Category *", category);
        addRow(form, 2, "Description * (min 10 chars)", new JScrollPane(description));
        addRow(form, 3, "District *", district);
        addRow(form, 4, "Specific place *", place);
        addRow(form, 5, (lost ? "Date lost" : "Date found") + " * (yyyy-MM-dd)", date);
        addRow(form, 6, lost ? "Reward (optional)" : "Currently kept at *", extra);

        JButton submit = new JButton(lost ? "Submit Lost Report" : "Register Found Item");
        JButton clear = new JButton("Clear");
        submit.addActionListener(e -> submit());
        clear.addActionListener(e -> clear());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(submit);
        buttons.add(clear);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 1; g.gridy = 7; g.anchor = GridBagConstraints.WEST;
        form.add(buttons, g);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrap.add(form);
        add(heading, BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
    }

    private void addRow(JPanel p, int row, String label, JComponent comp) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = row;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(5, 5, 5, 12);
        p.add(new JLabel(label), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        p.add(comp, g);
    }

    private void submit() {
        try {
            Item created;
            if (lost) {
                created = svc.reportLost(user, name.getText(), (Category) category.getSelectedItem(),
                        description.getText(), (District) district.getSelectedItem(),
                        place.getText(), date.getText(), extra.getText());
            } else {
                created = svc.registerFound(user, name.getText(), (Category) category.getSelectedItem(),
                        description.getText(), (District) district.getSelectedItem(),
                        place.getText(), date.getText(), extra.getText());
            }
            List<MatchResult> matches = svc.findMatches(created);
            clear();
            frame.dataChanged();
            Ui.info(this, "Report #" + created.getId() + " submitted successfully.");
            if (!matches.isEmpty()) Ui.showMatches(this, created, matches);
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void clear() {
        name.setText("");
        description.setText("");
        place.setText("");
        extra.setText("");
        category.setSelectedIndex(0);
        district.setSelectedIndex(0);
        date.setText(LocalDate.now().format(Validator.DATE_FMT));
    }
}
