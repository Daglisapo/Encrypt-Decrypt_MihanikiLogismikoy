package gr.ihu.omada12.crypto.controller.general;

import gr.ihu.omada12.crypto.model.settings.AppPaths;
import gr.ihu.omada12.crypto.model.settings.Settings;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.TutorialDialog;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

// Υλοποιεί: APP-06, APP-07, APP-08, APP-10 (απενεργοποίηση ιστορικού), APP-12, VLT-08 (UC-25)

/** Καρτέλα «Ρυθμίσεις»: κάθε αλλαγή αποθηκεύεται αμέσως και ισχύει και μετά την επανεκκίνηση. */
public class SettingsController {
    private final AppContext ctx;
    private Feedback feedback;

    @FXML private ComboBox<String> languageBox;
    @FXML private ComboBox<String> themeBox;
    @FXML private ColorPicker accentPicker;
    @FXML private Spinner<Integer> autoLockSpinner;
    @FXML private CheckBox historyCheck;
    @FXML private Label dataFolderLabel;
    @FXML private Label statusLabel;

    public SettingsController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        Settings s = ctx.getSettings();
        // πρώτα οι τιμές, μετά οι listeners, ώστε το γέμισμα να μη μετρά ως «αλλαγή»
        languageBox.getItems().setAll(List.of(ctx.getI18n().text("settings.langEl"), ctx.getI18n().text("settings.langEn")));
        languageBox.getSelectionModel().select("en".equals(s.getLanguage()) ? 1 : 0);
        themeBox.getItems().setAll(List.of(ctx.getI18n().text("settings.themeLight"), ctx.getI18n().text("settings.themeDark")));
        themeBox.getSelectionModel().select(s.getTheme() == Settings.Theme.DARK ? 1 : 0);
        accentPicker.setValue(Color.web(s.getAccent()));
        autoLockSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 120, s.getAutoLockMinutes()));
        historyCheck.setSelected(s.isHistoryEnabled());
        dataFolderLabel.setText(AppPaths.fromSystem().home().toString());

        languageBox.valueProperty().addListener((o, a, b) -> save());
        themeBox.valueProperty().addListener((o, a, b) -> save());
        accentPicker.valueProperty().addListener((o, a, b) -> save());
        autoLockSpinner.valueProperty().addListener((o, a, b) -> save());
        historyCheck.selectedProperty().addListener((o, a, b) -> save());
    }

    private void save() {
        Settings s = ctx.getSettings();
        s.setLanguage(languageBox.getSelectionModel().getSelectedIndex() == 1 ? "en" : "el");
        s.setTheme(themeBox.getSelectionModel().getSelectedIndex() == 1 ? Settings.Theme.DARK : Settings.Theme.LIGHT);
        Color c = accentPicker.getValue();
        s.setAccent(toHex(c.getRed(), c.getGreen(), c.getBlue()));
        s.setAutoLockMinutes(autoLockSpinner.getValue());
        s.setHistoryEnabled(historyCheck.isSelected());
        try {
            ctx.saveSettings(); // αν άλλαξε η γλώσσα, η οθόνη ξαναφορτώνεται με τα νέα κείμενα
            feedback.ok("settings.saved");
        } catch (IOException e) {
            feedback.errorKey("error.fileWrite", "settings.properties");
        }
    }

    @FXML private void showTutorial() { TutorialDialog.show(ctx); }

    /** Χρώμα (κανάλια 0.0-1.0) σε μορφή #rrggbb για το CSS. */
    private static String toHex(double red, double green, double blue) {
        return String.format(Locale.ROOT, "#%02x%02x%02x", Math.round(red * 255), Math.round(green * 255), Math.round(blue * 255));
    }
}
