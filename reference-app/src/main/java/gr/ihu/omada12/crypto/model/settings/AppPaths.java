package gr.ihu.omada12.crypto.model.settings;

import java.nio.file.Path;

// Υλοποιεί: APP-08, NFR-10, NFR-12

/**
 * Πού αποθηκεύει η εφαρμογή τα δεδομένα της: φάκελος .omada12-crypto στο home του χρήστη
 * (ίδιο σε Windows, Linux, macOS). Με -Domada12.home=... αλλάζει, π.χ. για δοκιμές.
 */
public final class AppPaths {
    private final Path home;

    public AppPaths(Path home) { this.home = home; }

    public static AppPaths fromSystem() {
        String custom = System.getProperty("omada12.home");
        if (custom != null && !custom.isBlank()) return new AppPaths(Path.of(custom));
        return new AppPaths(Path.of(System.getProperty("user.home"), ".omada12-crypto"));
    }

    public Path home() { return home; }

    public Path vaults() { return home.resolve("vaults"); }

    public Path history() { return home.resolve("history.log"); }

    public Path settings() { return home.resolve("settings.properties"); }
}
