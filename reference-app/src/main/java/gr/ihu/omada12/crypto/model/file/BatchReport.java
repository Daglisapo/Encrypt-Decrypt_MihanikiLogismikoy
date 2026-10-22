package gr.ihu.omada12.crypto.model.file;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Υλοποιεί: ENC-11

/** Αποτέλεσμα μαζικής κρυπτογράφησης: ποια αρχεία πέτυχαν και ποια απέτυχαν (με κλειδί μηνύματος). */
public class BatchReport {
    private final List<Path> succeeded = new ArrayList<>();
    private final Map<Path, String> failed = new LinkedHashMap<>();

    void addSuccess(Path output) { succeeded.add(output); }

    void addFailure(Path source, String messageKey) { failed.put(source, messageKey); }

    public List<Path> getSucceeded() { return Collections.unmodifiableList(succeeded); }

    public Map<Path, String> getFailed() { return Collections.unmodifiableMap(failed); }

    public int total() { return succeeded.size() + failed.size(); }
}
