package gr.ihu.omada12.crypto.tools;

import gr.ihu.omada12.crypto.view.FileSelection;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

// Υλοποιεί: (εργαλείο ανάπτυξης: «πατά» κουμπιά της πραγματικής διεπαφής για screenshots και έλεγχο)

/**
 * Μικρός «ρομποτικός χρήστης»: τρέχει σε δικό του νήμα και εκτελεί βήματα στο νήμα του JavaFX,
 * περιμένοντας να τελειώσει κάθε βήμα. Βρίσκει κόμβους με το fx:id τους στην τρέχουσα καρτέλα.
 */
public class UiRobot {
    private static final long TIMEOUT_MS = 60_000;

    private final Stage stage;

    public UiRobot(Stage stage) {
        this.stage = stage;
    }

    /** Εκτελεί κώδικα στο νήμα JavaFX και περιμένει να τελειώσει. */
    public void fx(Runnable action) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                done.countDown();
            }
        });
        done.await();
        if (error.get() != null) throw new IllegalStateException(error.get());
    }

    public void selectTab(int index) throws Exception {
        fx(() -> tabPane().getSelectionModel().select(index));
    }

    public void type(String id, String text) throws Exception {
        fx(() -> ((TextInputControl) find(id)).setText(text));
    }

    public void click(String id) throws Exception {
        fx(() -> ((ButtonBase) find(id)).fire());
    }

    public void choose(String labelId, Path file) throws Exception {
        fx(() -> ((FileSelection) find(labelId).getProperties().get(FileSelection.class)).set(file));
    }

    /** Περιμένει να ξεκλειδώσουν τα κουμπιά (#actions), δηλαδή να τελειώσει η εργασία παρασκηνίου. */
    public void waitIdle() throws Exception {
        long start = System.currentTimeMillis();
        AtomicReference<Boolean> busy = new AtomicReference<>(true);
        while (busy.get()) {
            if (System.currentTimeMillis() - start > TIMEOUT_MS) throw new IllegalStateException("timeout");
            Thread.sleep(100);
            fx(() -> busy.set(find("actions").isDisabled()));
        }
        fx(() -> { }); // ένας ακόμη κύκλος για να εφαρμοστούν οι αλλαγές στην οθόνη
    }

    public void shot(Path file) throws Exception {
        fx(() -> {
            WritableImage fx = stage.getScene().snapshot(null);
            int w = (int) fx.getWidth();
            int h = (int) fx.getHeight();
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            PixelReader reader = fx.getPixelReader();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) img.setRGB(x, y, reader.getArgb(x, y));
            }
            try {
                ImageIO.write(img, "png", file.toFile());
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /** Ο κόμβος με αυτό το fx:id στην τρέχουσα οθόνη (ή στην επιλεγμένη καρτέλα της). Μόνο στο νήμα JavaFX. */
    Node find(String id) {
        Node node = content().lookup("#" + id);
        if (node == null) throw new IllegalStateException("Δεν βρέθηκε #" + id);
        return node;
    }

    private Parent content() {
        TabPane tabs = tabPane();
        if (tabs != null) return (Parent) tabs.getSelectionModel().getSelectedItem().getContent();
        return (Parent) ((BorderPane) stage.getScene().getRoot()).getCenter();
    }

    private TabPane tabPane() {
        Node center = ((BorderPane) stage.getScene().getRoot()).getCenter();
        return center instanceof TabPane ? (TabPane) center : null;
    }
}
