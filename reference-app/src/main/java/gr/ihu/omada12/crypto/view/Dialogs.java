package gr.ihu.omada12.crypto.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

// Υλοποιεί: APP-03, APP-04, APP-05, APP-11

/** Τυποποιημένοι διάλογοι: επιβεβαίωση μη αναστρέψιμων ενεργειών, ενημέρωση, βοήθεια, εισαγωγή κειμένου. */
public class Dialogs {
    private final Stage owner;
    private final I18n i18n;

    public Dialogs(Stage owner, I18n i18n) {
        this.owner = owner;
        this.i18n = i18n;
    }

    /** APP-05: «Είστε σίγουροι;» με κουμπιά Ναι/Όχι στη γλώσσα του χρήστη. */
    public boolean confirm(String key, Object... args) {
        ButtonType yes = new ButtonType(i18n.text("common.yes"), ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType(i18n.text("common.no"), ButtonBar.ButtonData.NO);
        Alert alert = newAlert(true, i18n.text("common.confirmTitle"));
        alert.setContentText(i18n.text(key, args));
        alert.getButtonTypes().setAll(yes, no);
        return alert.showAndWait().filter(b -> b == yes).isPresent();
    }

    public void info(String key, Object... args) {
        Alert alert = newAlert(false, i18n.text("common.infoTitle"));
        alert.setContentText(i18n.text(key, args));
        alert.showAndWait();
    }

    public void help(String title, String text) {
        Alert alert = newAlert(false, i18n.text("nav.help") + " - " + title);
        TextArea area = new TextArea(text);
        area.setWrapText(true);
        area.setEditable(false);
        area.setPrefSize(520, 300);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    public Optional<String> askText(String key, String initial) {
        TextInputDialog dialog = new TextInputDialog(initial);
        dialog.initOwner(owner);
        dialog.setTitle(i18n.text("common.inputTitle"));
        dialog.setHeaderText(null);
        dialog.setContentText(i18n.text(key));
        return dialog.showAndWait().map(String::trim);
    }

    /** Νέο παράθυρο μηνύματος με τον τίτλο, τον «ιδιοκτήτη» και το θέμα εμφάνισης της εφαρμογής. */
    private Alert newAlert(boolean confirmation, String title) {
        Alert alert = new Alert(confirmation ? Alert.AlertType.CONFIRMATION : Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (owner.getScene() != null) alert.getDialogPane().getStylesheets().setAll(owner.getScene().getStylesheets());
        return alert;
    }
}
