package gr.ihu.omada12.crypto.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.nio.file.Path;

// Υλοποιεί: ENC-06 (σύρσιμο ή επιλογή από παράθυρο)

/**
 * Επαναχρησιμοποιούμενο «πεδίο αρχείου»: μια ετικέτα που δείχνει το αρχείο (και δέχεται drag & drop)
 * και ένα κουμπί «Επιλογή…». Το ίδιο χρησιμοποιείται σε κρυπτογράφηση, PDF, υπογραφές, στεγανογραφία.
 */
public class FileSelection {
    private final ObjectProperty<Path> path = new SimpleObjectProperty<>();
    private final Label label;
    private final I18n i18n;

    public FileSelection(AppContext ctx, Label label, Button choose, String titleKey, FileFilter... filters) {
        this.label = label;
        this.i18n = ctx.getI18n();
        label.getStyleClass().add("drop-zone");
        choose.setOnAction(e -> {
            Path p = ctx.files().open(titleKey, filters);
            if (p != null) set(p);
        });
        new DropZone(label, paths -> set(paths.get(0)));
        label.getProperties().put(FileSelection.class, this); // για τον αυτοματισμό (UiRobot)
        refresh();
    }

    public Path get() { return path.get(); }

    public void set(Path value) {
        path.set(value);
        refresh();
    }

    public ObjectProperty<Path> pathProperty() { return path; }

    private void refresh() {
        Path p = path.get();
        label.setText(p == null ? i18n.text("common.dropHere") : p.getFileName().toString());
    }
}
