package gr.ihu.omada12.crypto;

import gr.ihu.omada12.crypto.model.settings.AppPaths;
import gr.ihu.omada12.crypto.tools.ScreenshotTool;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.AutoLockMonitor;
import gr.ihu.omada12.crypto.view.ExitGuard;
import gr.ihu.omada12.crypto.view.Navigator;
import gr.ihu.omada12.crypto.view.Screen;
import gr.ihu.omada12.crypto.view.ThemeManager;
import gr.ihu.omada12.crypto.view.TutorialDialog;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

// Υλοποιεί: APP-01, APP-03, APP-12, VLT-08

/**
 * Στήνει την εφαρμογή: AppContext (κοινές υπηρεσίες), Navigator (οθόνες), θέμα, παρακολούθηση
 * αδράνειας και έξοδο με επιβεβαίωση. Με --screenshots=φάκελος τρέχει το σενάριο screenshots.
 * Είναι ξεχωριστή από το App ώστε να μετριέται από το MetricsCalculator (το App κληρονομεί
 * από το javafx Application, που το εργαλείο δεν αναγνωρίζει).
 */
public class AppStartup {
    private final Stage stage;
    private final String screenshotsDir;
    private AppContext ctx;

    public AppStartup(Stage stage, Map<String, String> options) {
        this.stage = stage;
        this.screenshotsDir = options.get("screenshots");
    }

    public void start() throws IOException {
        if (screenshotsDir != null) {
            System.setProperty("omada12.home", Files.createTempDirectory("omada12-shots").toString());
        }
        ctx = new AppContext(stage, AppPaths.fromSystem());
        Navigator navigator = new Navigator(ctx);
        ctx.setNavigator(navigator);
        Scene scene = new Scene(navigator.getRoot(), 1180, 780);
        stage.setScene(scene);
        new ThemeManager(scene).apply(ctx.getSettings());
        stage.setTitle(ctx.getI18n().text("app.title"));
        stage.setMinWidth(980);
        stage.setMinHeight(660);
        navigator.show(Screen.HOME);
        new AutoLockMonitor(scene, ctx).start();
        stage.setOnCloseRequest(e -> {
            if (!ExitGuard.confirmExit(ctx)) e.consume();
        });
        stage.show();

        if (screenshotsDir != null) {
            Platform.runLater(() -> new ScreenshotTool(ctx, Path.of(screenshotsDir)).run());
        } else if (!ctx.getSettings().isTutorialDone()) {
            Platform.runLater(this::firstRunTutorial);
        }
    }

    /** Κλείσιμο με οποιονδήποτε τρόπο: το θησαυροφυλάκιο ΔΕΝ μένει ποτέ ανοιχτό. */
    public void stop() {
        if (ctx != null) ctx.getVaultSession().lock();
    }

    /** APP-12: ο οδηγός εμφανίζεται μόνο στην πρώτη εκκίνηση. */
    private void firstRunTutorial() {
        TutorialDialog.show(ctx);
        ctx.getSettings().setTutorialDone(true);
        try {
            ctx.saveSettings();
        } catch (IOException ignored) {
            // αν δεν αποθηκευτεί, ο οδηγός απλώς θα ξαναεμφανιστεί την επόμενη φορά
        }
    }
}
