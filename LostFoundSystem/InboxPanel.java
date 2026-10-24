import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class InboxPanel extends JPanel implements Refreshable {
    private final LostFoundService svc;
    private final User user;
    private final MainFrame frame;
    private final DefaultTableModel model = Ui.model("From", "Regarding", "Received", "Status");
    private final JTable table = Ui.table(model);
    private final JTextArea detail = new JTextArea(7, 40);
    private List<Message> shown = new ArrayList<>();

    InboxPanel(LostFoundService svc, User user, MainFrame frame) {
        this.svc = svc;
        this.user = user;
        this.frame = frame;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        JScrollPane detailScroll = new JScrollPane(detail);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Message"));

        JButton reply = new JButton("Reply");
        reply.addActionListener(e -> reply());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(reply);

        JPanel south = new JPanel(new BorderLayout());
        south.add(detailScroll, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelected();
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        refresh();
    }

    @Override
    public void refresh() {
        shown = svc.inbox(user);
        model.setRowCount(0);
        detail.setText("");
        for (Message m : shown) {
            model.addRow(new Object[]{m.getFrom().getFullName(), m.getItem().toString(),
                    m.getSentAtText(), m.isRead() ? "Read" : "NEW"});
        }
    }

    private void showSelected() {
        int row = Ui.selectedRow(table);
        if (row < 0 || row >= shown.size()) return;
        Message m = shown.get(row);
        m.markRead();
        model.setValueAt("Read", row, 3);
        detail.setText("From: " + m.getFrom().getFullName() + " (" + m.getFrom().getRole()
                + ")  -  " + m.getFrom().getContact()
                + "\nRegarding: " + m.getItem()
                + "\nSent: " + m.getSentAtText()
                + "\n\n" + m.getText());
        detail.setCaretPosition(0);
        frame.updateInboxBadge();
    }

    private void reply() {
        int row = Ui.selectedRow(table);
        if (row < 0 || row >= shown.size()) {
            Ui.error(this, "Select a message to reply to.");
            return;
        }
        Message m = shown.get(row);
        String text = Ui.askText(this, "Reply", "Reply to " + m.getFrom().getFullName() + ":");
        if (text == null) return;
        try {
            svc.sendMessage(user, m.getFrom(), m.getItem(), text);
            Ui.info(this, "Reply sent.");
            frame.dataChanged();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}
