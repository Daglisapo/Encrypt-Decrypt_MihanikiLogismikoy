package gr.ihu.omada12.crypto.controller.lab;

import gr.ihu.omada12.crypto.model.analysis.CipherStep;
import gr.ihu.omada12.crypto.model.analysis.CipherStepper;
import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: LAB-09 (UC-23)

/**
 * Καρτέλα «Βήμα-βήμα»: δείχνει πώς κρυπτογραφείται κάθε γράμμα με Caesar ή Vigenère,
 * π.χ. «H (7) + 3 = K (10)». Ο χρήστης προχωρά ένα βήμα τη φορά ή βλέπει όλα μαζί.
 */
public class StepperController {
    private final AppContext ctx;
    private final CipherStepper stepper = new CipherStepper();
    private List<CipherStep> steps = new ArrayList<>();
    private int index;
    private Feedback feedback;

    @FXML private RadioButton caesarRadio;
    @FXML private TextField textField;
    @FXML private TextField keyField;
    @FXML private Label explanationLabel;
    @FXML private Label outputLabel;
    @FXML private TableView<CipherStep> stepsTable;
    @FXML private TableColumn<CipherStep, String> inputColumn;
    @FXML private TableColumn<CipherStep, String> keyColumn;
    @FXML private TableColumn<CipherStep, String> shiftColumn;
    @FXML private TableColumn<CipherStep, String> outputColumn;
    @FXML private Label statusLabel;

    public StepperController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        inputColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(letter(c.getValue().getInput())));
        keyColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getKeyLetter())));
        shiftColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper("+" + c.getValue().getShift()));
        outputColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(letter(c.getValue().getOutput())));
    }

    @FXML private void start() {
        try {
            steps = caesarRadio.isSelected()
                    ? stepper.caesar(textField.getText(), parseShift(keyField.getText()))
                    : stepper.vigenere(textField.getText(), keyField.getText());
            index = 0;
            stepsTable.getItems().clear();
            outputLabel.setText("");
            explanationLabel.setText(ctx.getI18n().text("lab.steps.ready", steps.size()));
            feedback.clear();
        } catch (CryptoException e) {
            feedback.error(e);
        }
    }

    @FXML private void next() {
        if (index >= steps.size()) {
            feedback.info("lab.steps.finished");
            return;
        }
        CipherStep s = steps.get(index++);
        stepsTable.getItems().add(s);
        stepsTable.scrollTo(s);
        outputLabel.setText(outputLabel.getText() + s.getOutput());
        String key = s.getKeyLetter() == ' ' ? String.valueOf(s.getShift()) : s.getKeyLetter() + " (" + s.getShift() + ")";
        explanationLabel.setText(letter(s.getInput()) + " + " + key + " = " + letter(s.getOutput()));
    }

    @FXML private void all() {
        while (index < steps.size()) next();
    }

    /** π.χ. «H (7)»: το γράμμα και η θέση του στο αλφάβητο (A=0). */
    private static String letter(char c) {
        return c + " (" + (Character.toUpperCase(c) - 'A') + ")";
    }

    private static int parseShift(String text) throws CryptoException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new CryptoException("error.caesarNotNumber");
        }
    }
}
