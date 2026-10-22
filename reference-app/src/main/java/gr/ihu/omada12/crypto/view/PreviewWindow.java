package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.vault.PreviewKind;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: VLT-14

/**
 * Προεπισκόπηση εικόνας ή κειμένου ΑΠΟ ΤΗ ΜΝΗΜΗ: τα bytes δεν γράφονται ποτέ στον δίσκο.
 * Κλείνει αυτόματα όταν κλειδώσει το θησαυροφυλάκιο (closeAll).
 */
public final class PreviewWindow {
    private static final int MAX_TEXT_CHARS = 200_000;
    private static final List<Stage> OPEN = new ArrayList<>();

    private PreviewWindow() { }

    public static Stage show(AppContext ctx, String name, byte[] data, PreviewKind kind) {
        Stage window = new Stage();
        window.initOwner(ctx.getStage());
        window.initModality(Modality.NONE);
        window.setTitle(ctx.getI18n().text("vault.preview.title", name));
        ScrollPane content;
        if (kind == PreviewKind.IMAGE) {
            ImageView view = new ImageView(new Image(new ByteArrayInputStream(data)));
            view.setPreserveRatio(true);
            view.setFitWidth(800);
            content = new ScrollPane(view);
        } else {
            String text = new String(data, StandardCharsets.UTF_8);
            TextArea area = new TextArea(text.length() > MAX_TEXT_CHARS ? text.substring(0, MAX_TEXT_CHARS) : text);
            area.setEditable(false);
            area.setWrapText(true);
            content = new ScrollPane(area);
            content.setFitToWidth(true);
            content.setFitToHeight(true);
        }
        Scene scene = new Scene(content, 820, 600);
        scene.getStylesheets().setAll(ctx.getStage().getScene().getStylesheets());
        window.setScene(scene);
        window.setOnHidden(e -> OPEN.remove(window));
        OPEN.add(window);
        window.show();
        return window;
    }

    /** Κλείνει όλες τις προεπισκοπήσεις όταν κλειδώνει το θησαυροφυλάκιο. */
    public static void closeAll() {
        for (Stage s : new ArrayList<>(OPEN)) s.close();
        OPEN.clear();
    }
}
