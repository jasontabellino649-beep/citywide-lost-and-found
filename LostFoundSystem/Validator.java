import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

final class Validator {
    static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Validator() { }

    static String requireText(String field, String value, int min, int max) throws ValidationException {
        String t = value == null ? "" : value.trim();
        if (t.length() < min) {
            throw new ValidationException(field + " must be at least " + min + " characters.");
        }
        if (t.length() > max) {
            throw new ValidationException(field + " must not exceed " + max + " characters.");
        }
        return t;
    }

    static LocalDate parseDate(String field, String value) throws ValidationException {
        String t = value == null ? "" : value.trim();
        LocalDate d;
        try {
            d = LocalDate.parse(t, DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field + " must use the format yyyy-MM-dd (e.g. 2026-10-24).");
        }
        if (d.isAfter(LocalDate.now())) {
            throw new ValidationException(field + " cannot be in the future.");
        }
        return d;
    }

    static String requireContact(String value) throws ValidationException {
        String t = requireText("Contact", value, 5, 60);
        boolean email = t.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
        boolean phone = t.matches("^\\+?[0-9][0-9 \\-]{6,17}$");
        if (!email && !phone) {
            throw new ValidationException("Contact must be a valid email address or phone number.");
        }
        return t;
    }
}
