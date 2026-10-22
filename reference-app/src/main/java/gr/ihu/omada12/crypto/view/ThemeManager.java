package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.settings.Settings;
import javafx.scene.Scene;

// Υλοποιεί: APP-07

/** Εφαρμόζει φωτεινό/σκοτεινό θέμα (αρχεία CSS) και το χρώμα έμφασης του χρήστη σε μια σκηνή. */
public class ThemeManager {
    private static final String BASE = "/gr/ihu/omada12/crypto/css/base.css";
    private static final String LIGHT = "/gr/ihu/omada12/crypto/css/light.css";
    private static final String DARK = "/gr/ihu/omada12/crypto/css/dark.css";

    private final Scene scene;

    public ThemeManager(Scene scene) {
        this.scene = scene;
    }

    public void apply(Settings settings) {
        scene.getStylesheets().setAll(css(BASE), css(settings.getTheme() == Settings.Theme.DARK ? DARK : LIGHT));
        String accent = settings.getAccent(); // ήδη ελεγμένο ως #RRGGBB από το Settings
        scene.getRoot().setStyle("-o12-accent: " + accent + "; -fx-accent: " + accent + "; -fx-focus-color: " + accent + ";");
    }

    private static String css(String path) {
        return ThemeManager.class.getResource(path).toExternalForm();
    }
}
