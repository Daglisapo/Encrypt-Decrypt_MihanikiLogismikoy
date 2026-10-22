package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.history.HistoryRepository;
import gr.ihu.omada12.crypto.model.history.HistoryService;
import gr.ihu.omada12.crypto.model.settings.AppPaths;
import gr.ihu.omada12.crypto.model.settings.Settings;
import gr.ihu.omada12.crypto.model.settings.SettingsRepository;
import gr.ihu.omada12.crypto.model.vault.InactivityTimer;
import gr.ihu.omada12.crypto.model.vault.LoginThrottle;
import gr.ihu.omada12.crypto.model.vault.VaultManager;
import gr.ihu.omada12.crypto.model.vault.VaultSession;
import gr.ihu.omada12.crypto.model.vault.VaultStore;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

// Υλοποιεί: APP-06, APP-07, APP-08 (κοινές υπηρεσίες για όλους τους controllers)

/**
 * Το «κουτί» με τις κοινές υπηρεσίες που χρειάζονται οι controllers: ρυθμίσεις, ιστορικό,
 * θησαυροφυλάκια, κείμενα, πλοήγηση, διάλογοι. Δημιουργείται μία φορά στο App και δίνεται
 * σε κάθε controller από το ControllerFactory. Δεν περιέχει λογική εφαρμογής.
 */
public class AppContext {
    private final Stage stage;
    private final SettingsRepository settingsRepository;
    private final Settings settings;
    private final HistoryService history;
    private final VaultManager vaults;
    private final VaultSession vaultSession;
    private I18n i18n;
    private Navigator navigator;

    public AppContext(Stage stage, AppPaths paths) {
        this.stage = stage;
        this.settingsRepository = new SettingsRepository(paths.settings());
        this.settings = settingsRepository.load();
        this.history = new HistoryService(new HistoryRepository(paths.history()));
        this.vaults = new VaultManager(paths.vaults(), new VaultStore(), new LoginThrottle(Clock.systemUTC()));
        this.vaultSession = new VaultSession(new InactivityTimer(Clock.systemUTC(), Duration.ZERO));
        this.i18n = new I18n(settings.getLanguage());
        applyRuntimeSettings();
    }

    public Stage getStage() { return stage; }

    public Settings getSettings() { return settings; }

    public HistoryService getHistory() { return history; }

    public VaultManager getVaults() { return vaults; }

    public VaultSession getVaultSession() { return vaultSession; }

    public I18n getI18n() { return i18n; }

    public Navigator getNavigator() { return navigator; }

    public void setNavigator(Navigator navigator) { this.navigator = navigator; }

    public Dialogs dialogs() { return new Dialogs(stage, i18n); }

    public FileDialogs files() { return new FileDialogs(stage, i18n); }

    /** APP-08: αποθήκευση και άμεση εφαρμογή (θέμα, ιστορικό, αυτόματο κλείδωμα, γλώσσα). */
    public void saveSettings() throws IOException {
        settingsRepository.save(settings);
        boolean languageChanged = !i18n.getLocale().getLanguage().equals(settings.getLanguage());
        applyRuntimeSettings();
        if (stage.getScene() != null) new ThemeManager(stage.getScene()).apply(settings);
        if (languageChanged) {
            i18n = new I18n(settings.getLanguage());
            stage.setTitle(i18n.text("app.title"));
            if (navigator != null) navigator.reload();
        }
    }

    private void applyRuntimeSettings() {
        history.setEnabled(settings.isHistoryEnabled());
        vaultSession.getTimer().setTimeout(Duration.ofMinutes(settings.getAutoLockMinutes()));
    }
}
