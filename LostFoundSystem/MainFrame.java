import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class MainFrame extends JFrame {
    private final LostFoundService svc;
    private final User user;
    private final JTabbedPane tabs = new JTabbedPane();
    private int inboxIndex = -1;

    MainFrame(LostFoundService svc, User user) {
        super("Citywide Lost & Found Management System");
        this.svc = svc;
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { DataStore.save(svc); }
        });

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Ui.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("Citywide Lost & Found");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JLabel who = new JLabel(user.getFullName() + "  |  " + user.getRole());
        who.setForeground(Color.WHITE);
        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> {
            DataStore.save(svc);
            dispose();
            new LoginFrame(svc).setVisible(true);
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(who);
        right.add(logout);
        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        // tabs shared by everyone
        tabs.addTab("Browse & Search", new BrowsePanel(svc, user, this));
        tabs.addTab("Report Lost Item", new ReportFormPanel(svc, user, this, true));
        tabs.addTab("Register Found Item", new ReportFormPanel(svc, user, this, false));
        tabs.addTab("My Activity", new MyActivityPanel(svc, user, this));
        inboxIndex = tabs.getTabCount();
        tabs.addTab("Inbox", new InboxPanel(svc, user, this));

        // admin-only tabs (POLYMORPHISM: decided by the user object)
        if (user.canModerate()) {
            tabs.addTab("Claims Review", new ClaimsReviewPanel(svc, user, this));
            tabs.addTab("Statistics", new StatsPanel(svc));
            tabs.addTab("Users", new UsersPanel(svc));
        }

        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Refreshable) ((Refreshable) c).refresh();
        });

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        updateInboxBadge();
        setSize(1180, 720);
        setLocationRelativeTo(null);
    }

    /** Called by any panel after data changes: saves and refreshes every tab. */
    void dataChanged() {
        DataStore.save(svc);
        for (int i = 0; i < tabs.getTabCount(); i++) {
            Component c = tabs.getComponentAt(i);
            if (c instanceof Refreshable) ((Refreshable) c).refresh();
        }
        updateInboxBadge();
    }

    void updateInboxBadge() {
        int n = svc.unreadCount(user);
        tabs.setTitleAt(inboxIndex, n > 0 ? "Inbox (" + n + ")" : "Inbox");
    }
}
