import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

final class DataStore {
    private static final File FILE = new File("lostfound.dat");

    private DataStore() { }

    static LostFoundService load() {
        if (FILE.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE))) {
                return (LostFoundService) in.readObject();
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                System.err.println("Could not read saved data, starting fresh: " + e.getMessage());
            }
        }
        return LostFoundService.withSampleData();
    }

    static void save(LostFoundService service) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE))) {
            out.writeObject(service);
        } catch (IOException e) {
            System.err.println("Could not save data: " + e.getMessage());
        }
    }
}
