import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

class UsersPanel extends JPanel implements Refreshable {
    private final LostFoundService svc;
    private final DefaultTableModel model = Ui.model("ID", "Full name", "Username", "Role", "Contact");

    UsersPanel(LostFoundService svc) {
        this.svc = svc;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(Ui.table(model)), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        for (User u : svc.getUsers()) {
            model.addRow(new Object[]{u.getId(), u.getFullName(), u.getUsername(), u.getRole(), u.getContact()});
        }
    }
}
