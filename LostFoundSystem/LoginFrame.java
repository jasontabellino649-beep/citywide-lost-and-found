import javax.swing.*;
import java.awt.*;

class LoginFrame extends JFrame {
    private final LostFoundService svc;
    private final JTextField username = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);

    LoginFrame(LostFoundService svc) {
        super("Citywide Lost & Found - Sign in");
        this.svc = svc;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel banner = new JPanel(new GridLayout(2, 1));
        banner.setBackground(Ui.PRIMARY);
        banner.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Citywide Lost & Found");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JLabel sub = new JLabel("Reunite people with their belongings across the whole city");
        sub.setForeground(new Color(210, 225, 240));
        banner.add(title);
        banner.add(sub);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Username:"), g);
        g.gridx = 1; form.add(username, g);
        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Password:"), g);
        g.gridx = 1; form.add(password, g);

        JButton login = new JButton("Sign in");
        JButton register = new JButton("Create account");
        login.addActionListener(e -> doLogin());
        register.addActionListener(e -> doRegister());
        getRootPane().setDefaultButton(login);

        JLabel hint = new JLabel("Demo: admin/admin123  |  user/user123  |  maria/maria123");
        hint.setForeground(Color.GRAY);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        buttons.add(login);
        buttons.add(register);
        JPanel south = new JPanel(new BorderLayout());
        south.add(buttons, BorderLayout.NORTH);
        JPanel hintRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        hintRow.add(hint);
        south.add(hintRow, BorderLayout.SOUTH);

        add(banner, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        try {
            User u = svc.login(username.getText(), new String(password.getPassword()));
            dispose();
            new MainFrame(svc, u).setVisible(true);
        } catch (AuthenticationException ex) {
            Ui.error(this, ex.getMessage());
            password.setText("");
        }
    }

    private void doRegister() {
        JTextField fullName = new JTextField(18);
        JTextField contact = new JTextField(18);
        JTextField user = new JTextField(18);
        JPasswordField pw = new JPasswordField(18);
        JPasswordField pw2 = new JPasswordField(18);
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Full name:"));            p.add(fullName);
        p.add(new JLabel("Email or phone:"));       p.add(contact);
        p.add(new JLabel("Username:"));             p.add(user);
        p.add(new JLabel("Password (6+ chars):"));  p.add(pw);
        p.add(new JLabel("Confirm password:"));     p.add(pw2);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, p, "Create account",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;
            try {
                svc.register(user.getText(), new String(pw.getPassword()), new String(pw2.getPassword()),
                        fullName.getText(), contact.getText());
                DataStore.save(svc);
                username.setText(user.getText().trim());
                Ui.info(this, "Account created! You can now sign in.");
                return;
            } catch (ValidationException ex) {
                Ui.error(this, ex.getMessage());
            }
        }
    }
}
