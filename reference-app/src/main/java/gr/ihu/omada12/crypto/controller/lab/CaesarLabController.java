package gr.ihu.omada12.crypto.controller.lab;

import gr.ihu.omada12.crypto.model.analysis.BruteForceAnalyzer;
import gr.ihu.omada12.crypto.model.analysis.Candidate;
import gr.ihu.omada12.crypto.model.analysis.FrequencyAnalyzer;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.Formats;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.util.List;

// Υλοποιεί: LAB-05, LAB-06 (UC-23)

/**
 * Καρτέλα «Κρυπτανάλυση Caesar»: και οι 25 μετατοπίσεις, ταξινομημένες με χ², με την
 * πιθανότερη επισημασμένη, και ραβδόγραμμα συχνοτήτων γραμμάτων σε σύγκριση με τα αγγλικά.
 */
public class CaesarLabController {
    static final String SAMPLE = "Wkh txlfn eurzq ira mxpsv ryhu wkh odcb grj";

    private final AppContext ctx;
    private final BruteForceAnalyzer analyzer = new BruteForceAnalyzer();
    private final FrequencyAnalyzer frequencies = new FrequencyAnalyzer();
    private Feedback feedback;

    @FXML private TextArea cipherText;
    @FXML private TableView<Candidate> candidates;
    @FXML private TableColumn<Candidate, String> shiftColumn;
    @FXML private TableColumn<Candidate, String> textColumn;
    @FXML private TableColumn<Candidate, String> scoreColumn;
    @FXML private BarChart<String, Number> frequencyChart;
    @FXML private Label statusLabel;

    public CaesarLabController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        shiftColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getShift())));
        textColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getText()));
        scoreColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formats.number(c.getValue().getScore(), 1)));
        // η πρώτη γραμμή (μικρότερο χ²) είναι η πιθανότερη και επισημαίνεται
        candidates.setRowFactory(t -> new TableRow<Candidate>() {
            @Override protected void updateItem(Candidate c, boolean empty) {
                super.updateItem(c, empty);
                getStyleClass().remove("best-row");
                if (!empty && c != null && getIndex() == 0) getStyleClass().add("best-row");
            }
        });
        frequencyChart.setAnimated(false);
    }

    @FXML private void sample() { cipherText.setText(SAMPLE); }

    @FXML private void crack() {
        String text = cipherText.getText();
        if (text == null || text.isBlank()) {
            feedback.errorKey("error.emptyText");
            return;
        }
        List<Candidate> list = analyzer.crack(text);
        candidates.setItems(FXCollections.observableArrayList(list));
        showFrequencies(list.get(0).getText());
        feedback.ok("lab.caesar.best", list.get(0).getShift());
        ctx.getHistory().record(AppModule.LAB, "action.caesarCrack", "", true);
    }

    /** LAB-06: συχνότητες της πιθανότερης λύσης δίπλα στις αναμενόμενες των αγγλικών. */
    private void showFrequencies(String text) {
        double[] observed = frequencies.percentages(text);
        XYChart.Series<String, Number> textSeries = new XYChart.Series<>();
        textSeries.setName(ctx.getI18n().text("lab.caesar.textSeries"));
        XYChart.Series<String, Number> english = new XYChart.Series<>();
        english.setName(ctx.getI18n().text("lab.caesar.englishSeries"));
        for (int i = 0; i < 26; i++) {
            String letter = String.valueOf((char) ('A' + i));
            textSeries.getData().add(new XYChart.Data<>(letter, observed[i]));
            english.getData().add(new XYChart.Data<>(letter, FrequencyAnalyzer.ENGLISH[i]));
        }
        frequencyChart.getData().setAll(List.of(textSeries, english));
    }
}
