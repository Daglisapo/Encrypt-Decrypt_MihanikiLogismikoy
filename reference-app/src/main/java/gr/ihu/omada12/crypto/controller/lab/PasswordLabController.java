package gr.ihu.omada12.crypto.controller.lab;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.password.PasswordGenerator;
import gr.ihu.omada12.crypto.model.password.PasswordPolicy;
import gr.ihu.omada12.crypto.model.password.PasswordStrength;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.Formats;
import gr.ihu.omada12.crypto.view.StrengthIndicator;
import gr.ihu.omada12.crypto.view.Views;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

// Υλοποιεί: LAB-07, LAB-08 (UC-24)

/**
 * Καρτέλα «Κωδικοί»: έλεγχος ισχύος με εκτίμηση χρόνου «σπασίματος» και γεννήτρια ισχυρών κωδικών.
 * Ό,τι πληκτρολογείται εδώ δεν αποθηκεύεται πουθενά (ούτε στο ιστορικό).
 */
public class PasswordLabController {
    private final AppContext ctx;
    private final PasswordGenerator generator = new PasswordGenerator();
    private Feedback feedback;
    private StrengthIndicator indicator;

    @FXML private TextField testField;
    @FXML private ProgressBar strengthBar;
    @FXML private Label levelLabel;
    @FXML private Label entropyLabel;
    @FXML private Label hintsLabel;
    @FXML private Spinner<Integer> lengthSpinner;
    @FXML private CheckBox lowerCheck;
    @FXML private CheckBox upperCheck;
    @FXML private CheckBox digitsCheck;
    @FXML private CheckBox symbolsCheck;
    @FXML private TextField generatedField;
    @FXML private Label statusLabel;

    public PasswordLabController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        lengthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                PasswordPolicy.MIN_LENGTH, PasswordPolicy.MAX_LENGTH, 16));
        indicator = new StrengthIndicator(ctx.getI18n(), testField, strengthBar, levelLabel);
        testField.textProperty().addListener((o, a, b) -> evaluate());
        evaluate();
    }

    /** LAB-07 */
    private void evaluate() {
        PasswordStrength s = indicator.show(testField.getText());
        boolean empty = testField.getText().isEmpty();
        entropyLabel.setText(empty ? "" : ctx.getI18n().text("lab.pw.entropy", Formats.number(s.getEntropyBits(), 1)));
        StringBuilder hints = new StringBuilder();
        for (String key : s.getHintKeys()) hints.append("• ").append(ctx.getI18n().text(key)).append('\n');
        hintsLabel.setText(empty ? "" : hints.toString().trim());
    }

    /** LAB-08 */
    @FXML private void generate() {
        PasswordPolicy policy = new PasswordPolicy(lengthSpinner.getValue(), lowerCheck.isSelected(),
                upperCheck.isSelected(), digitsCheck.isSelected(), symbolsCheck.isSelected());
        try {
            String pw = generator.generate(policy);
            generatedField.setText(pw);
            testField.setText(pw);
            feedback.ok("lab.pw.generated");
        } catch (CryptoException e) {
            feedback.error(e);
        }
    }

    @FXML private void copy() {
        Views.copyToClipboard(generatedField.getText());
        feedback.ok("enc.text.copied");
    }
}
