package gr.ihu.omada12.crypto.controller.lab;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.io.FileProcessor;
import gr.ihu.omada12.crypto.model.lab.BenchmarkAlgorithm;
import gr.ihu.omada12.crypto.model.lab.BenchmarkCatalog;
import gr.ihu.omada12.crypto.model.lab.BenchmarkCsv;
import gr.ihu.omada12.crypto.model.lab.BenchmarkResult;
import gr.ihu.omada12.crypto.model.lab.BenchmarkRunner;
import gr.ihu.omada12.crypto.view.ActionRunner;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Feedback;
import gr.ihu.omada12.crypto.view.FileSelection;
import gr.ihu.omada12.crypto.view.Formats;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: LAB-01, LAB-02, LAB-03, LAB-04 (UC-22)

/**
 * Καρτέλα «Σύγκριση»: αλγόριθμοι σε αρχείο του χρήστη, N επαναλήψεις, πίνακας μέσων όρων,
 * ραβδόγραμμα ταχύτητας και εξαγωγή CSV.
 */
public class BenchmarkController {
    private final AppContext ctx;
    private final BenchmarkRunner benchmark = new BenchmarkRunner();
    private final List<CheckBox> algorithmChecks = new ArrayList<>();
    private List<BenchmarkAlgorithm> algorithms = new ArrayList<>();
    private List<BenchmarkResult> results = new ArrayList<>();
    private FileSelection dataFile;
    private Feedback feedback;
    private ActionRunner runner;

    @FXML private Label fileLabel;
    @FXML private Button chooseFile;
    @FXML private VBox algorithmBox;
    @FXML private Spinner<Integer> repetitions;
    @FXML private TableView<BenchmarkResult> resultsTable;
    @FXML private TableColumn<BenchmarkResult, String> algorithmColumn;
    @FXML private TableColumn<BenchmarkResult, String> encryptColumn;
    @FXML private TableColumn<BenchmarkResult, String> decryptColumn;
    @FXML private TableColumn<BenchmarkResult, String> speedColumn;
    @FXML private TableColumn<BenchmarkResult, String> outputColumn;
    @FXML private BarChart<String, Number> chart;
    @FXML private ProgressBar progressBar;
    @FXML private Button cancelButton;
    @FXML private Pane actions;
    @FXML private Label statusLabel;

    public BenchmarkController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private void initialize() {
        feedback = new Feedback(statusLabel, ctx.getI18n());
        runner = new ActionRunner(ctx, AppModule.LAB, feedback, actions, progressBar, cancelButton);
        dataFile = new FileSelection(ctx, fileLabel, chooseFile, "lab.bench.chooseTitle");
        repetitions.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, BenchmarkRunner.MAX_REPETITIONS, 5));
        try {
            algorithms = BenchmarkCatalog.all();
            for (BenchmarkAlgorithm a : algorithms) {
                CheckBox check = new CheckBox(a.getName());
                check.setSelected(true);
                algorithmChecks.add(check);
            }
            algorithmBox.getChildren().addAll(algorithmChecks);
        } catch (CryptoException e) {
            feedback.error(e);
        }
        algorithmColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getAlgorithm()));
        encryptColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formats.number(c.getValue().getEncryptMillis(), 2)));
        decryptColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formats.number(c.getValue().getDecryptMillis(), 2)));
        speedColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formats.number(c.getValue().getEncryptMBps(), 1)));
        outputColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formats.size(c.getValue().getOutputBytes())));
        chart.setAnimated(false);
    }

    @FXML private void run() {
        Path file = dataFile.get();
        if (file == null) {
            feedback.errorKey("error.noFileSelected");
            return;
        }
        List<BenchmarkAlgorithm> chosen = new ArrayList<>();
        for (int i = 0; i < algorithms.size(); i++) {
            if (algorithmChecks.get(i).isSelected()) chosen.add(algorithms.get(i));
        }
        if (chosen.size() < 2) {
            feedback.errorKey("lab.bench.selectTwo");
            return;
        }
        int n = repetitions.getValue();
        runner.run("action.benchmark", file.getFileName().toString(),
                p -> benchmark.run(chosen, new FileProcessor().read(file), n, p), this::show);
    }

    private void show(List<BenchmarkResult> list) {
        results = list;
        resultsTable.setItems(FXCollections.observableArrayList(list));
        XYChart.Series<String, Number> enc = new XYChart.Series<>();
        enc.setName(ctx.getI18n().text("lab.bench.encSpeed"));
        XYChart.Series<String, Number> dec = new XYChart.Series<>();
        dec.setName(ctx.getI18n().text("lab.bench.decSpeed"));
        for (BenchmarkResult r : list) {
            enc.getData().add(new XYChart.Data<>(r.getAlgorithm(), r.getEncryptMBps()));
            dec.getData().add(new XYChart.Data<>(r.getAlgorithm(), r.getDecryptMBps()));
        }
        chart.getData().setAll(List.of(enc, dec));
        feedback.ok("lab.bench.done", list.size(), repetitions.getValue());
    }

    /** LAB-04 */
    @FXML private void exportCsv() {
        if (results.isEmpty()) {
            feedback.errorKey("lab.bench.noResults");
            return;
        }
        Path target = ctx.files().save("lab.bench.csvTitle", "benchmark.csv", ctx.files().filter("filter.csv", "*.csv"));
        if (target == null) return;
        try {
            new BenchmarkCsv().write(results, target);
            feedback.ok("common.savedTo", target.getFileName());
        } catch (CryptoException e) {
            feedback.error(e);
        }
    }
}
