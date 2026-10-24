import javax.swing.*;

enum Category {
    ELECTRONICS("Electronics"), BAGS_WALLETS("Bags & Wallets"), DOCUMENTS("Documents & IDs"),
    KEYS("Keys"), CLOTHING("Clothing & Accessories"), JEWELRY("Jewelry & Watches"),
    PETS("Pets"), OTHER("Other");

    private final String label;
    Category(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
