package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.io.ProgressListener;
import javafx.concurrent.Task;

import java.util.function.Consumer;

// Υλοποιεί: ENC-10, NFR-06 (η διεπαφή δεν «παγώνει» σε βαριές εργασίες)

/**
 * Τρέχει μια εργασία του μοντέλου σε νήμα παρασκηνίου (PBKDF2, μεγάλα αρχεία, μετρήσεις).
 * Η πρόοδος του μοντέλου γίνεται progressProperty της Task, για να δεθεί σε ProgressBar,
 * και το task.cancel() φτάνει στο μοντέλο μέσω του ProgressListener.isCancelled().
 */
public final class TaskRunner {

    /** Η δουλειά που γίνεται στο παρασκήνιο. */
    public interface Work<T> {
        T run(ProgressListener progress) throws Exception;
    }

    private TaskRunner() { }

    public static <T> Task<T> start(Work<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        ModelTask<T> task = new ModelTask<>(work);
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        task.setOnCancelled(e -> onError.accept(new CryptoException("error.cancelled")));
        Thread thread = new Thread(task, "omada12-worker");
        thread.setDaemon(true);
        thread.start();
        return task;
    }

    /** Task που «μεταφράζει» την πρόοδο του μοντέλου σε πρόοδο JavaFX. */
    private static final class ModelTask<T> extends Task<T> {
        private final Work<T> work;

        ModelTask(Work<T> work) { this.work = work; }

        @Override
        protected T call() throws Exception {
            return work.run(new ProgressListener() {
                @Override public void onProgress(long done, long total) { updateProgress(done, total); }
                @Override public boolean isCancelled() { return ModelTask.this.isCancelled(); }
            });
        }
    }
}
