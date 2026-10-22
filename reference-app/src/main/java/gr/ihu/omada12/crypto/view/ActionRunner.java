package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.history.AppModule;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;

import java.util.function.Consumer;

// Υλοποιεί: ENC-10, APP-04, APP-09 (κοινή ροή για κάθε ενέργεια χρήστη)

/**
 * Η κοινή ροή κάθε ενέργειας: κλείδωμα κουμπιών → εργασία στο παρασκήνιο (με πρόοδο/ακύρωση)
 * → μήνυμα επιτυχίας ή σφάλματος → εγγραφή στο ιστορικό (μόνο όνομα αρχείου και αποτέλεσμα).
 * Έτσι οι controllers γράφουν μόνο «τι» γίνεται, όχι «πώς».
 */
public class ActionRunner {
    private final AppContext ctx;
    private final AppModule module;
    private final Feedback feedback;
    private final Node controls;
    private final ProgressBar progressBar;
    private final Button cancelButton;
    private Task<?> running;

    public ActionRunner(AppContext ctx, AppModule module, Feedback feedback, Node controls) {
        this(ctx, module, feedback, controls, null, null);
    }

    /** Με μπάρα προόδου και κουμπί ακύρωσης (ENC-10). */
    public ActionRunner(AppContext ctx, AppModule module, Feedback feedback, Node controls,
                        ProgressBar progressBar, Button cancelButton) {
        this.ctx = ctx;
        this.module = module;
        this.feedback = feedback;
        this.controls = controls;
        this.progressBar = progressBar;
        this.cancelButton = cancelButton;
        if (cancelButton != null) {
            cancelButton.setOnAction(e -> cancel());
            cancelButton.setDisable(true);
        }
    }

    /**
     * @param actionKey κλειδί ενέργειας για το ιστορικό (null = να μη γραφτεί)
     * @param fileName  μόνο το όνομα του αρχείου (ή κενό) — ΠΟΤΕ κωδικός ή περιεχόμενο
     */
    public <T> void run(String actionKey, String fileName, TaskRunner.Work<T> work, Consumer<T> onSuccess) {
        setBusy(true);
        feedback.info("common.working");
        running = TaskRunner.start(work, result -> {
            setBusy(false);
            feedback.clear();
            onSuccess.accept(result);
            record(actionKey, fileName, true);
        }, error -> {
            setBusy(false);
            feedback.error(error);
            record(actionKey, fileName, false);
        });
        if (progressBar != null) progressBar.progressProperty().bind(running.progressProperty());
    }

    public void cancel() {
        if (running != null) running.cancel();
    }

    private void record(String actionKey, String fileName, boolean success) {
        if (actionKey != null) ctx.getHistory().record(module, actionKey, fileName == null ? "" : fileName, success);
    }

    private void setBusy(boolean busy) {
        controls.setDisable(busy);
        if (cancelButton != null) cancelButton.setDisable(!busy);
        if (!busy && progressBar != null) {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        }
    }
}
