package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.password.PasswordStrength;
import gr.ihu.omada12.crypto.model.password.PasswordStrengthMeter;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputControl;

// Υλοποιεί: VLT-01 (ένδειξη ισχύος), LAB-07

/** Δένει ένα πεδίο κωδικού με μπάρα ισχύος και ετικέτα (επίπεδο + χρόνος «σπασίματος»). */
public class StrengthIndicator {
    private final PasswordStrengthMeter meter = new PasswordStrengthMeter();
    private final I18n i18n;
    private final ProgressBar bar;
    private final Label label;

    public StrengthIndicator(I18n i18n, TextInputControl field, ProgressBar bar, Label label) {
        this.i18n = i18n;
        this.bar = bar;
        this.label = label;
        field.textProperty().addListener((obs, old, value) -> show(value));
        show(field.getText());
    }

    /** Ενημερώνει μπάρα και ετικέτα και επιστρέφει την εκτίμηση (για όποιον θέλει περισσότερα στοιχεία). */
    public PasswordStrength show(String password) {
        PasswordStrength s = meter.evaluate(password);
        boolean empty = password == null || password.isEmpty();
        bar.setProgress(empty ? 0 : s.getRatio());
        for (int i = 0; i <= 4; i++) bar.getStyleClass().remove("strength-" + i);
        bar.getStyleClass().add("strength-" + s.getScore());
        label.setText(empty ? "" : i18n.text(s.getLevelKey()) + " · " + Formats.crackTime(i18n, s.getCrackTime()));
        return s;
    }
}
