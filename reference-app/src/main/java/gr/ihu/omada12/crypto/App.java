package gr.ihu.omada12.crypto;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

// Υλοποιεί: APP-01 (σημείο εκκίνησης JavaFX)

/**
 * Σημείο εκκίνησης JavaFX. Μένει σκόπιμα πολύ μικρό: όλη η δουλειά γίνεται στο AppStartup.
 * Παράμετρος: --screenshots=φάκελος (σενάριο screenshots για το Παραδοτέο 2, βλ. README).
 */
public class App extends Application {
    private AppStartup startup;

    @Override
    public void start(Stage stage) throws IOException {
        startup = new AppStartup(stage, getParameters().getNamed());
        startup.start();
    }

    @Override
    public void stop() {
        if (startup != null) startup.stop();
    }
}
