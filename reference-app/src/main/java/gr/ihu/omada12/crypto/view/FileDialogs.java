package gr.ihu.omada12.crypto.view;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: ENC-06, VLT-03, VLT-06 (παράθυρα επιλογής αρχείου/φακέλου)

/**
 * Παράθυρα επιλογής αρχείων με τίτλους από το bundle. Επιστρέφουν null όταν ο χρήστης πατήσει «Άκυρο».
 * Το παράθυρο αποθήκευσης του λειτουργικού ζητά ήδη επιβεβαίωση αντικατάστασης (APP-05).
 */
public class FileDialogs {
    private static File lastDirectory;

    private final Stage owner;
    private final I18n i18n;

    public FileDialogs(Stage owner, I18n i18n) {
        this.owner = owner;
        this.i18n = i18n;
    }

    public FileFilter filter(String key, String... patterns) {
        return new FileFilter(key, patterns);
    }

    public Path open(String titleKey, FileFilter... filters) {
        return remember(chooser(titleKey, null, filters).showOpenDialog(owner));
    }

    public List<Path> openMany(String titleKey) {
        List<File> files = chooser(titleKey, null).showOpenMultipleDialog(owner);
        List<Path> result = new ArrayList<>();
        if (files != null) {
            for (File f : files) result.add(remember(f));
        }
        return result;
    }

    public Path save(String titleKey, String initialName, FileFilter... filters) {
        return remember(chooser(titleKey, initialName, filters).showSaveDialog(owner));
    }

    public Path directory(String titleKey) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(i18n.text(titleKey));
        if (lastDirectory != null && lastDirectory.isDirectory()) chooser.setInitialDirectory(lastDirectory);
        File dir = chooser.showDialog(owner);
        if (dir != null) lastDirectory = dir;
        return dir == null ? null : dir.toPath();
    }

    private FileChooser chooser(String titleKey, String initialName, FileFilter... filters) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n.text(titleKey));
        for (FileFilter f : filters) {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(i18n.text(f.getDescriptionKey()), f.getPatterns()));
        }
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(i18n.text("filter.all"), "*.*"));
        if (initialName != null) chooser.setInitialFileName(initialName);
        if (lastDirectory != null && lastDirectory.isDirectory()) chooser.setInitialDirectory(lastDirectory);
        return chooser;
    }

    private static Path remember(File file) {
        if (file == null) return null;
        lastDirectory = file.getParentFile();
        return file.toPath();
    }
}
