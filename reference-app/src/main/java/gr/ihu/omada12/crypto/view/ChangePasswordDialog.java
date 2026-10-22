package gr.ihu.omada12.crypto.view;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;

import java.util.Optional;

// Υλοποιεί: VLT-10

/** Διάλογος αλλαγής κύριου κωδικού: τρέχων, νέος, επιβεβαίωση νέου, με ένδειξη ισχύος. */
public final class ChangePasswordDialog {

    /** Οι τρεις κωδικοί που έδωσε ο χρήστης. */
    public static final class Result {
        private final char[] current;
        private final char[] next;
        private final char[] confirm;

        Result(String current, String next, String confirm) {
            this.current = current.toCharArray();
            this.next = next.toCharArray();
            this.confirm = confirm.toCharArray();
        }

        public char[] getCurrent() { return current; }

        public char[] getNext() { return next; }

        public char[] getConfirm() { return confirm; }
    }

    private ChangePasswordDialog() { }

    public static Optional<Result> ask(AppContext ctx) {
        I18n i18n = ctx.getI18n();
        Dialog<Result> dialog = new Dialog<>();
        dialog.initOwner(ctx.getStage());
        dialog.setTitle(i18n.text("vault.changePw.title"));
        dialog.getDialogPane().getStylesheets().setAll(ctx.getStage().getScene().getStylesheets());

        PasswordField current = new PasswordField();
        PasswordField next = new PasswordField();
        PasswordField confirm = new PasswordField();
        ProgressBar bar = new ProgressBar(0);
        Label strength = new Label();
        new StrengthIndicator(i18n, next, bar, strength);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label(i18n.text("vault.changePw.current")), current);
        grid.addRow(1, new Label(i18n.text("vault.changePw.new")), next);
        grid.addRow(2, new Label(i18n.text("vault.changePw.confirm")), confirm);
        grid.addRow(3, bar, strength);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(b -> b == ButtonType.OK ? new Result(current.getText(), next.getText(), confirm.getText()) : null);
        return dialog.showAndWait();
    }
}
