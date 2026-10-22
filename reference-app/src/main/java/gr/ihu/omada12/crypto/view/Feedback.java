package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import javafx.scene.control.Label;

// Υλοποιεί: APP-04

/**
 * Γραμμή μηνυμάτων κάτω από κάθε φόρμα: πράσινο για επιτυχία, κόκκινο για σφάλμα.
 * Τα σφάλματα του μοντέλου έρχονται ως CryptoException με κλειδί bundle, άρα το κείμενο
 * είναι πάντα κατανοητό και στη γλώσσα του χρήστη.
 */
public class Feedback {
    private final Label label;
    private final I18n i18n;

    public Feedback(Label label, I18n i18n) {
        this.label = label;
        this.i18n = i18n;
        label.setWrapText(true);
        clear();
    }

    public void ok(String key, Object... args) { show(i18n.text(key, args), "status-ok"); }

    public void info(String key, Object... args) { show(i18n.text(key, args), "status-info"); }

    public void error(Throwable t) {
        if (t instanceof CryptoException) {
            show(i18n.error((CryptoException) t), "status-error");
        } else {
            show(i18n.text("error.unexpected", String.valueOf(t.getMessage())), "status-error");
        }
    }

    public void errorKey(String key, Object... args) { show(i18n.text(key, args), "status-error"); }

    public void clear() { show("", "status-info"); }

    private void show(String text, String style) {
        label.setText(text);
        label.getStyleClass().removeAll("status-ok", "status-error", "status-info");
        label.getStyleClass().add(style);
    }
}
