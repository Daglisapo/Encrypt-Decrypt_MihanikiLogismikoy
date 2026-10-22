package gr.ihu.omada12.crypto.view;

import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Υλοποιεί: ENC-06, VLT-03

/**
 * Κάνει έναν κόμβο της διεπαφής «ζώνη ρίψης» αρχείων (drag & drop από την Εξερεύνηση).
 * Ο κόμβος περνά μόνο στον κατασκευαστή: οι μέθοδοι δεν έχουν παραμέτρους τύπου JavaFX,
 * ώστε το MetricsCalculator να υπολογίζει κανονικά τις μετρικές της κλάσης.
 */
public class DropZone {
    private final Node target;
    private final Consumer<List<Path>> onDrop;

    public DropZone(Node target, Consumer<List<Path>> onDrop) {
        this.target = target;
        this.onDrop = onDrop;
        target.addEventHandler(DragEvent.DRAG_OVER, e -> {
            if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.COPY);
            e.consume();
        });
        target.addEventHandler(DragEvent.DRAG_ENTERED, e -> highlight(true));
        target.addEventHandler(DragEvent.DRAG_EXITED, e -> highlight(false));
        target.addEventHandler(DragEvent.DRAG_DROPPED, e -> {
            boolean ok = e.getDragboard().hasFiles();
            if (ok) drop(e.getDragboard().getFiles());
            e.setDropCompleted(ok);
            e.consume();
        });
    }

    private void drop(List<File> files) {
        List<Path> paths = new ArrayList<>();
        for (File f : files) paths.add(f.toPath());
        onDrop.accept(paths);
    }

    private void highlight(boolean on) {
        if (on) target.getStyleClass().add("drop-hover");
        else target.getStyleClass().remove("drop-hover");
    }
}
