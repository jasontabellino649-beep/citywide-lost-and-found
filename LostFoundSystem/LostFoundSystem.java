import javax.swing.*;

/*
 * ============================================================
 *  CITYWIDE LOST & FOUND MANAGEMENT SYSTEM  (Java Swing)
 * ============================================================
 *  Run:   javac LostFoundSystem.java
 *         java LostFoundSystem
 *  Demo accounts:  admin / admin123   (Administrator)
 *                  user  / user123    (Citizen)
 *                  maria / maria123   (Citizen)
 *
 *  FEATURES
 *   1. Report lost items            4. Contact the reporter (in-app messages)
 *   2. Register found items         5. Claim found items + admin approval
 *   3. Search with filters          6. Smart match suggestions
 *   7. Status tracking              8. Admin: review claims, statistics, users
 *   9. Data saved automatically to lostfound.dat
 *
 *  OOP PILLARS
 *   Encapsulation : private fields + getters, rules hidden in LostFoundService
 *   Inheritance   : User -> RegularUser/Admin, Item -> LostItem/FoundItem
 *   Polymorphism  : getType(), getExtraInfo(), isClaimable(), canModerate()
 *                   behave differently per subclass
 *   Abstraction   : abstract classes User and Item, interfaces Searchable
 *                   and Refreshable
 * ============================================================
 */

public class LostFoundSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // fall back to the default look and feel
            }
            LostFoundService service = DataStore.load();
            new LoginFrame(service).setVisible(true);
        });
    }
}
