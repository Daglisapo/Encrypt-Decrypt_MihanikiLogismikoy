package gr.ihu.omada12.crypto.tools;

import gr.ihu.omada12.crypto.model.settings.Settings;
import gr.ihu.omada12.crypto.view.AppContext;
import gr.ihu.omada12.crypto.view.Screen;
import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Υλοποιεί: (εργαλείο ανάπτυξης για τα screenshots του Παραδοτέου 2 και αυτόματο έλεγχο της διεπαφής)

/**
 * Εκτελεί ένα σενάριο πάνω στην ΠΡΑΓΜΑΤΙΚΗ διεπαφή (πατά κουμπιά, γράφει κείμενα) και σώζει
 * screenshots με ονόματα της μορφής SS-UCxx-nn_περιγραφή.png. Δουλεύει σε προσωρινό φάκελο
 * δεδομένων. Τερματίζει με κωδικό 0 μόνο αν όλα τα βήματα πέτυχαν, άρα λειτουργεί και ως έλεγχος.
 */
public class ScreenshotTool {
    private final AppContext ctx;
    private final Path outDir;
    private final UiRobot robot;
    private final DemoData demo;
    private int count;

    public ScreenshotTool(AppContext ctx, Path outDir) {
        this.ctx = ctx;
        this.outDir = outDir;
        this.robot = new UiRobot(ctx.getStage());
        this.demo = new DemoData(Path.of(System.getProperty("omada12.home"), "demo"));
    }

    /** Καλείται από το νήμα JavaFX· το σενάριο τρέχει σε δικό του νήμα. */
    public void run() {
        Thread script = new Thread(() -> {
            try {
                Files.createDirectories(outDir);
                demo.create(ctx.getVaults());
                encryption();
                vault();
                signature();
                stego();
                lab();
                general();
                System.out.println("SCREENSHOTS OK: " + count + " -> " + outDir.toAbsolutePath());
                System.exit(0);
            } catch (Exception | Error e) {
                e.printStackTrace();
                System.exit(1);
            }
        }, "screenshot-script");
        script.setDaemon(true);
        script.start();
    }

    private void encryption() throws Exception {
        show(Screen.HOME);
        shot("SS-UI01_arxiki");
        show(Screen.ENCRYPTION);
        robot.type("passwordField", "Demo-Pass-2026");
        robot.type("inputArea", "Καλημέρα! Αυτό είναι ένα μυστικό μήνυμα.");
        robot.click("encryptButton");
        robot.waitIdle();
        shot("SS-UC01-01_kryptografisi_AES");
        robot.click("swapButton");
        robot.type("passwordField", "lathos-kodikos");
        robot.click("decryptButton");
        robot.waitIdle();
        shot("SS-UC02-02_lathos_kodikos");
        robot.type("passwordField", "Demo-Pass-2026");
        robot.click("decryptButton");
        robot.waitIdle();
        shot("SS-UC02-01_apokryptografisi");
        algorithm("Caesar");
        robot.type("keyField", "3");
        robot.type("inputArea", "HELLO");
        robot.click("encryptButton");
        robot.waitIdle();
        shot("SS-UC01-02_caesar_HELLO_KHOOR");
        robot.type("inputArea", "");
        robot.click("encryptButton");
        robot.waitIdle();
        shot("SS-UC01-03_keno_keimeno");
        robot.selectTab(1);
        shot("SS-UC03-01_arxeia");
        robot.selectTab(2);
        shot("SS-UC05-01_pdf");
    }

    private void vault() throws Exception {
        show(Screen.VAULT_LIST);
        shot("SS-UC07-01_thisavrofylakia");
        ctx.getVaultSession().open(ctx.getVaults().unlock("Demo", DemoData.VAULT_PASSWORD));
        show(Screen.VAULT);
        shot("SS-UC10-01_periexomena");
        robot.type("searchField", "pass");
        shot("SS-UC10-02_anazitisi");
    }

    private void signature() throws Exception {
        show(Screen.SIGNATURE);
        shot("SS-UC16-01_kleidia");
        robot.selectTab(2);
        robot.choose("documentLabel", demo.contract());
        robot.choose("signatureLabel", demo.signature());
        robot.choose("publicKeyLabel", demo.publicKey());
        robot.click("verifyButton");
        robot.waitIdle();
        shot("SS-UC18-01_egkyri_ypografi");
        demo.tamperContract();
        robot.click("verifyButton");
        robot.waitIdle();
        shot("SS-UC18-02_mi_egkyri_ypografi");
        robot.selectTab(3);
        robot.choose("fileALabel", demo.contractCopy());
        robot.waitIdle();
        robot.choose("fileBLabel", demo.contract());
        robot.waitIdle();
        robot.click("compareButton");
        shot("SS-UC19-01_sygkrisi_SHA256");
    }

    private void stego() throws Exception {
        show(Screen.STEGO);
        robot.choose("coverLabel", demo.cover());
        robot.type("messageArea", "Το μήνυμα κρύβεται στα λιγότερο σημαντικά bits.");
        shot("SS-UC20-01_xoritikotita");
        robot.selectTab(1);
        robot.choose("imageLabel", demo.stegoImage());
        robot.click("revealButton");
        robot.waitIdle();
        shot("SS-UC21-01_anaktisi");
    }

    private void lab() throws Exception {
        show(Screen.LAB);
        robot.choose("fileLabel", demo.benchFile());
        robot.click("runButton");
        robot.waitIdle();
        shot("SS-UC22-01_sygkrisi_algorithmon");
        robot.selectTab(1);
        robot.click("sampleButton");
        robot.click("crackButton");
        shot("SS-UC23-01_brute_force");
        robot.selectTab(2);
        robot.type("testField", "123456");
        shot("SS-UC24-01_adynamos_kodikos");
        robot.click("generateButton");
        shot("SS-UC24-02_gennitria");
        robot.selectTab(3);
        robot.click("startButton");
        robot.click("allButton");
        shot("SS-UC23-02_vima_vima");
    }

    private void general() throws Exception {
        show(Screen.GENERAL);
        shot("SS-UC25-01_rythmiseis");
        robot.selectTab(1);
        shot("SS-UC26-01_istoriko");
        robot.fx(() -> {
            ctx.getSettings().setTheme(Settings.Theme.DARK);
            ctx.getSettings().setLanguage("en");
            try {
                ctx.saveSettings();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
        show(Screen.HOME);
        shot("SS-UC25-02_dark_english");
        show(Screen.VAULT);
        shot("SS-UC25-03_dark_vault");
    }

    @SuppressWarnings("unchecked")
    private void algorithm(String name) throws Exception {
        robot.fx(() -> ((ComboBox<String>) robot.find("algorithmBox")).setValue(name));
    }

    private void show(Screen screen) throws Exception {
        robot.fx(() -> ctx.getNavigator().show(screen));
    }

    private void shot(String name) throws Exception {
        robot.shot(outDir.resolve(name + ".png"));
        count++;
    }
}
