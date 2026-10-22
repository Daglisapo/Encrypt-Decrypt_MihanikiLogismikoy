package gr.ihu.omada12.crypto.view;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

// Υλοποιεί: APP-12

/** Οδηγός πρώτης χρήσης: λίγες σελίδες με «Πίσω / Επόμενο», που εμφανίζονται στην πρώτη εκκίνηση. */
public final class TutorialDialog {
    public static final int PAGES = 5;

    private TutorialDialog() { }

    public static void show(AppContext ctx) {
        I18n i18n = ctx.getI18n();
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(ctx.getStage());
        dialog.setTitle(i18n.text("tutorial.title"));
        dialog.getDialogPane().getStylesheets().setAll(ctx.getStage().getScene().getStylesheets());

        Label heading = new Label();
        heading.getStyleClass().add("section-title");
        Label body = new Label();
        body.setWrapText(true);
        body.setPrefWidth(460);
        Label pageNo = new Label();
        VBox box = new VBox(12, heading, body, pageNo);
        box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);

        ButtonType prev = new ButtonType(i18n.text("tutorial.prev"), ButtonBar.ButtonData.BACK_PREVIOUS);
        ButtonType next = new ButtonType(i18n.text("tutorial.next"), ButtonBar.ButtonData.NEXT_FORWARD);
        ButtonType close = new ButtonType(i18n.text("tutorial.close"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(prev, next, close);

        int[] page = {1};
        Runnable render = () -> {
            heading.setText(i18n.text("tutorial." + page[0] + ".title"));
            body.setText(i18n.text("tutorial." + page[0] + ".body"));
            pageNo.setText(page[0] + " / " + PAGES);
            dialog.getDialogPane().lookupButton(prev).setDisable(page[0] == 1);
            dialog.getDialogPane().lookupButton(next).setDisable(page[0] == PAGES);
        };
        // Τα Πίσω/Επόμενο αλλάζουν σελίδα χωρίς να κλείνουν τον διάλογο
        ((Button) dialog.getDialogPane().lookupButton(prev)).addEventFilter(ActionEvent.ACTION, e -> {
            page[0]--;
            render.run();
            e.consume();
        });
        ((Button) dialog.getDialogPane().lookupButton(next)).addEventFilter(ActionEvent.ACTION, e -> {
            page[0]++;
            render.run();
            e.consume();
        });
        render.run();
        dialog.showAndWait();
    }
}
