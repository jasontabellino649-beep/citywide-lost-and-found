import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final int id;
    private final User from;
    private final User to;
    private final Item item;
    private final String text;
    private final LocalDateTime sentAt = LocalDateTime.now();
    private boolean read;

    Message(int id, User from, User to, Item item, String text) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.item = item;
        this.text = text;
    }

    public int getId() { return id; }
    public User getFrom() { return from; }
    public User getTo() { return to; }
    public Item getItem() { return item; }
    public String getText() { return text; }
    public String getSentAtText() { return sentAt.format(FMT); }
    public boolean isRead() { return read; }
    void markRead() { this.read = true; }
}
