import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class LostFoundService implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<User> users = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<Claim> claims = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();
    private int idCounter = 1;

    // ---------- accounts ----------
    public User register(String username, String password, String confirm,
                         String fullName, String contact) throws ValidationException {
        String u = Validator.requireText("Username", username, 4, 20);
        if (!u.matches("[A-Za-z0-9_]+")) {
            throw new ValidationException("Username may only contain letters, numbers and underscores.");
        }
        String name = Validator.requireText("Full name", fullName, 2, 50);
        String c = Validator.requireContact(contact);
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
        if (!password.equals(confirm)) {
            throw new ValidationException("Passwords do not match.");
        }
        if (findUser(u) != null) {
            throw new ValidationException("That username is already taken.");
        }
        User user = new RegularUser(idCounter++, u, password, name, c);
        users.add(user);
        return user;
    }

    public User login(String username, String password) throws AuthenticationException {
        User u = findUser(username == null ? "" : username.trim());
        if (u == null || !u.checkPassword(password)) {
            throw new AuthenticationException("Invalid username or password.");
        }
        return u;
    }

    private User findUser(String username) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    // ---------- reporting ----------
    public LostItem reportLost(User reporter, String name, Category cat, String desc,
                               District district, String place, String dateText,
                               String reward) throws ValidationException {
        String n = Validator.requireText("Item name", name, 2, 60);
        String d = Validator.requireText("Description", desc, 10, 500);
        Location loc = new Location(district, Validator.requireText("Specific place", place, 3, 80));
        LocalDate date = Validator.parseDate("Date lost", dateText);
        String r = reward == null ? "" : reward.trim();
        if (r.length() > 60) throw new ValidationException("Reward text must not exceed 60 characters.");
        LostItem item = new LostItem(idCounter++, reporter, n, cat, d, loc, date, r);
        items.add(item);
        return item;
    }

    public FoundItem registerFound(User reporter, String name, Category cat, String desc,
                                   District district, String place, String dateText,
                                   String keptAt) throws ValidationException {
        String n = Validator.requireText("Item name", name, 2, 60);
        String d = Validator.requireText("Description", desc, 10, 500);
        Location loc = new Location(district, Validator.requireText("Specific place", place, 3, 80));
        LocalDate date = Validator.parseDate("Date found", dateText);
        String k = Validator.requireText("Currently kept at", keptAt, 3, 80);
        FoundItem item = new FoundItem(idCounter++, reporter, n, cat, d, loc, date, k);
        items.add(item);
        return item;
    }

    // ---------- searching & matching ----------
    public List<Item> search(SearchFilter filter) {
        List<Item> out = new ArrayList<>();
        for (Item i : items) {
            if (i.matches(filter)) out.add(i);
        }
        Collections.reverse(out);   // newest first
        return out;
    }

    public List<MatchResult> findMatches(Item item) {
        List<MatchResult> out = new ArrayList<>();
        for (Item other : items) {
            if (other == item) continue;
            if (other.getType().equals(item.getType())) continue;        // need opposite type
            if (other.getStatus() != ItemStatus.OPEN) continue;
            if (other.getCategory() != item.getCategory()) continue;     // category must agree
            int score = 40;
            if (other.getLocation().getDistrict() == item.getLocation().getDistrict()) score += 25;
            long days = Math.abs(ChronoUnit.DAYS.between(other.getDate(), item.getDate()));
            if (days <= 3) score += 20;
            else if (days <= 14) score += 10;
            Set<String> shared = keywords(item);
            shared.retainAll(keywords(other));
            score += Math.min(15, 5 * shared.size());
            if (score >= 50) out.add(new MatchResult(other, score));
        }
        out.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return out;
    }

    private Set<String> keywords(Item i) {
        Set<String> set = new HashSet<>();
        for (String w : (i.getName() + " " + i.getDescription()).toLowerCase().split("[^a-z0-9]+")) {
            if (w.length() >= 4) set.add(w);
        }
        return set;
    }

    // ---------- claims ----------
    public Claim submitClaim(User claimant, Item item, String proof) throws ValidationException {
        if (!item.isClaimable()) {
            throw new ValidationException("This item cannot be claimed (" + item.getType()
                    + " report, status: " + item.getStatus() + ").");
        }
        if (item.getReporter().getId() == claimant.getId()) {
            throw new ValidationException("You cannot claim an item that you registered yourself.");
        }
        for (Claim c : claims) {
            if (c.getItem() == item && c.getClaimant().getId() == claimant.getId()
                    && c.getStatus() == ClaimStatus.PENDING) {
                throw new ValidationException("You already have a pending claim for this item.");
            }
        }
        String p = Validator.requireText("Proof of ownership", proof, 10, 500);
        Claim claim = new Claim(idCounter++, item, claimant, p);
        claims.add(claim);
        item.setStatus(ItemStatus.CLAIM_PENDING);
        return claim;
    }

    public void reviewClaim(User actor, Claim claim, boolean approve, String note) throws ValidationException {
        if (!actor.canModerate()) {
            throw new ValidationException("Only administrators can review claims.");
        }
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ValidationException("This claim has already been reviewed.");
        }
        String n = note == null ? "" : note.trim();
        if (n.length() > 200) throw new ValidationException("Note must not exceed 200 characters.");

        claim.review(approve, n);
        Item item = claim.getItem();
        String decision = approve ? "APPROVED" : "REJECTED";
        messages.add(new Message(idCounter++, actor, claim.getClaimant(), item,
                "Your claim for " + item + " was " + decision + "." + (n.isEmpty() ? "" : " Note: " + n)));

        if (approve) {
            item.setStatus(ItemStatus.CLAIMED);
            for (Claim other : claims) {
                if (other != claim && other.getItem() == item && other.getStatus() == ClaimStatus.PENDING) {
                    other.review(false, "Another claim was approved for this item.");
                }
            }
            messages.add(new Message(idCounter++, actor, item.getReporter(), item,
                    "A claim for your found item " + item + " was approved. Please arrange the handover with "
                            + claim.getClaimant().getFullName() + " (" + claim.getClaimant().getContact() + ")."));
        } else if (!hasPendingClaim(item)) {
            item.setStatus(ItemStatus.OPEN);
        }
    }

    private boolean hasPendingClaim(Item item) {
        for (Claim c : claims) {
            if (c.getItem() == item && c.getStatus() == ClaimStatus.PENDING) return true;
        }
        return false;
    }

    // ---------- messaging ----------
    public void sendMessage(User from, User to, Item item, String text) throws ValidationException {
        if (from.getId() == to.getId()) {
            throw new ValidationException("You cannot send a message to yourself.");
        }
        String t = Validator.requireText("Message", text, 5, 500);
        messages.add(new Message(idCounter++, from, to, item, t));
    }

    public List<Message> inbox(User user) {
        List<Message> out = new ArrayList<>();
        for (Message m : messages) {
            if (m.getTo().getId() == user.getId()) out.add(m);
        }
        Collections.reverse(out);
        return out;
    }

    public int unreadCount(User user) {
        int n = 0;
        for (Message m : messages) {
            if (m.getTo().getId() == user.getId() && !m.isRead()) n++;
        }
        return n;
    }

    // ---------- status tracking & admin actions ----------
    public void closeReport(User actor, Item item) throws ValidationException {
        boolean owner = item.getReporter().getId() == actor.getId();
        if (!owner && !actor.canModerate()) {
            throw new ValidationException("You can only close your own reports.");
        }
        if (item.getStatus() == ItemStatus.CLAIMED || item.getStatus() == ItemStatus.CLOSED) {
            throw new ValidationException("This report is already completed.");
        }
        item.setStatus(ItemStatus.CLOSED);
    }

    public void setStatus(User actor, Item item, ItemStatus status) throws ValidationException {
        if (!actor.canModerate()) throw new ValidationException("Only administrators can change status.");
        item.setStatus(status);
    }

    public void deleteItem(User actor, Item item) throws ValidationException {
        if (!actor.canModerate()) throw new ValidationException("Only administrators can delete reports.");
        items.remove(item);
        claims.removeIf(c -> c.getItem() == item);
        messages.removeIf(m -> m.getItem() == item);
    }

    // ---------- queries ----------
    public List<Item> itemsBy(User user) {
        List<Item> out = new ArrayList<>();
        for (Item i : items) {
            if (i.getReporter().getId() == user.getId()) out.add(i);
        }
        Collections.reverse(out);
        return out;
    }

    public List<Claim> claimsBy(User user) {
        List<Claim> out = new ArrayList<>();
        for (Claim c : claims) {
            if (c.getClaimant().getId() == user.getId()) out.add(c);
        }
        Collections.reverse(out);
        return out;
    }

    public List<Item> getItems() { return Collections.unmodifiableList(items); }
    public List<User> getUsers() { return Collections.unmodifiableList(users); }

    public List<Claim> getClaims() {
        List<Claim> out = new ArrayList<>(claims);
        Collections.reverse(out);
        return out;
    }

    // ---------- sample data ----------
    static LostFoundService withSampleData() {
        LostFoundService s = new LostFoundService();
        try {
            s.users.add(new Admin(s.idCounter++, "admin", "admin123", "City Administrator", "admin@lostfound.city"));
            User u1 = s.register("user", "user123", "user123", "Demo Citizen", "user@example.com");
            User u2 = s.register("maria", "maria123", "maria123", "Maria Santos", "0917-555-0101");

            s.reportLost(u1, "Black leather wallet", Category.BAGS_WALLETS,
                    "Black bi-fold leather wallet with a transit card and a library card inside.",
                    District.TRANSIT, "Central Bus Terminal, Gate 3", daysAgo(2), "Reward offered");
            s.reportLost(u2, "Silver smartphone", Category.ELECTRONICS,
                    "Silver smartphone with a blue rubber case and a cracked corner.",
                    District.MALLS, "Level 2 Food Court", daysAgo(1), "");
            s.reportLost(u1, "House keys with red tag", Category.KEYS,
                    "Three house keys on a ring with a red plastic tag.",
                    District.PARKS, "Riverside Park jogging path", daysAgo(5), "");

            s.registerFound(u2, "Black wallet", Category.BAGS_WALLETS,
                    "Black leather wallet found on a bench, contains cards and some cash.",
                    District.TRANSIT, "Central Bus Terminal waiting area", daysAgo(1), "Terminal information desk");
            s.registerFound(u1, "Blue phone case with phone", Category.ELECTRONICS,
                    "Smartphone with a blue rubber case, left on a table.",
                    District.MALLS, "Level 2 Food Court", daysAgo(1), "With me - contact via app");
            s.registerFound(u2, "Passport holder", Category.DOCUMENTS,
                    "Brown passport holder with a passport inside, found near the arrivals area.",
                    District.AIRPORT, "Arrivals Hall, Exit B", daysAgo(3), "Airport Police Desk");
            s.registerFound(u1, "Umbrella and tote bag", Category.BAGS_WALLETS,
                    "Green canvas tote bag containing a folded black umbrella.",
                    District.MARKET, "Fruit stalls, Aisle 4", daysAgo(4), "Market office");
        } catch (ValidationException e) {
            throw new IllegalStateException("Sample data is invalid: " + e.getMessage(), e);
        }
        return s;
    }

    private static String daysAgo(int n) {
        return LocalDate.now().minusDays(n).format(Validator.DATE_FMT);
    }
}
