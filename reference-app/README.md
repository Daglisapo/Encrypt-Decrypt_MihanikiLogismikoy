# Ομάδα 12 — Εργαλείο Ασφάλειας Δεδομένων (εφαρμογή αναφοράς)

Desktop εφαρμογή σε **Java 17 + JavaFX** (MVC, FXML, CSS) με όλες τις 58 λειτουργίες του εγγράφου
«Λειτουργίες Εφαρμογής – Ομάδα 12» (M, S και C) και τις 12 μη λειτουργικές απαιτήσεις.
Είναι **πρότυπο** για την ομάδα: δείχνει μια πλήρη λύση, όχι αυτό που πρέπει να παραδοθεί αυτούσιο.

- 6 ενότητες: Κρυπτογράφηση, Secure Vault, Ψηφιακή υπογραφή, Στεγανογραφία, Εργαστήριο, Γενικά
- Μόνο `javax.crypto` / `java.security`. Μοναδική εξωτερική βιβλιοθήκη: **Apache PDFBox** (κλείδωμα PDF)
- AES-256-GCM (τυχαίο IV 12 bytes), PBKDF2-HMAC-SHA256 με 600.000 επαναλήψεις, Ed25519 για υπογραφές
- Ελληνικά / Αγγλικά (ResourceBundle), φωτεινό / σκοτεινό θέμα, χωρίς internet, χωρίς βάση δεδομένων
- **90 JUnit 5 tests**, όλα πράσινα (`mvn clean test`)

Για την αρχιτεκτονική και τις σχεδιαστικές επιλογές διαβάστε το [SXEDIASH.md](SXEDIASH.md).

---

## 1. Τι χρειάζεται (Windows)

| Εργαλείο | Έκδοση | Πώς |
|---|---|---|
| JDK | 17 ή νεότερο (δοκιμάστηκε με Temurin/Oracle 17) | `winget install EclipseAdoptium.Temurin.17.JDK` ή https://adoptium.net |
| Maven | 3.8+ | `winget install Apache.Maven`, ή το Maven που έχει μέσα το IntelliJ |
| IntelliJ IDEA | Community ή Ultimate | προαιρετικό, αλλά προτείνεται |

Δεν χρειάζεται να εγκαταστήσετε JavaFX: το κατεβάζει το Maven.

Έλεγχος σε τερματικό (PowerShell ή cmd):

```bash
java -version
```

```bash
mvn -version
```

Το `mvn -version` πρέπει να δείχνει **Java version: 17** (ή νεότερη). Αν δείχνει παλαιότερη, ορίστε τη μεταβλητή
περιβάλλοντος `JAVA_HOME` στον φάκελο του JDK 17 (π.χ. `C:\Program Files\Java\jdk-17`) και ανοίξτε νέο τερματικό.

## 2. Εκτέλεση από τερματικό

Από τον φάκελο `reference-app`:

```bash
mvn clean test
```

```bash
mvn javafx:run
```

Η πρώτη φορά αργεί λίγο (κατέβασμα βιβλιοθηκών). Στην πρώτη εκκίνηση εμφανίζεται ο οδηγός πρώτης χρήσης (APP-12).

### Σημαντικό: φάκελος με ελληνικά στο όνομα

Στα Windows το `java.exe` δεν δέχεται ελληνικούς χαρακτήρες στο classpath όταν η κωδικοσελίδα του συστήματος είναι
Cp1252. Αν το project βρίσκεται π.χ. στο `…\Υπολογιστής\…`, το `mvn javafx:run` θα έβγαζε
«Could not find or load main class». Γι' αυτό το `pom.xml` έχει το profile **`windows-ascii-build`**, που ενεργοποιείται
μόνο του στα Windows και γράφει τα αρχεία build στο `%USERPROFILE%\.m2\omada12-build\crypto-app` αντί για `target\`.
Δεν χρειάζεται να κάνετε τίποτα. Για να το απενεργοποιήσετε (π.χ. σε φάκελο χωρίς ελληνικά):

```bash
mvn -P !windows-ascii-build javafx:run
```

## 3. Εκτέλεση από IntelliJ IDEA

1. **File → Open…** και επιλέξτε το `reference-app/pom.xml` → **Open as Project**.
2. **File → Project Structure → Project → SDK**: επιλέξτε JDK 17 (ή «Download JDK…»).
3. Περιμένετε να τελειώσει το «Maven import» (κάτω δεξιά). Αν χρειαστεί: παράθυρο **Maven** → κουμπί ⟳ *Reload*.
4. Εκτέλεση εφαρμογής, με έναν από τους δύο τρόπους:
   - παράθυρο **Maven → Plugins → javafx → javafx:run** (διπλό κλικ) — ο πιο σίγουρος τρόπος, ή
   - άνοιγμα της κλάσης `gr.ihu.omada12.crypto.Launcher` → πράσινο ▶ δίπλα στο `main`.
     (Τρέξτε το `Launcher`, όχι το `App`: έτσι αποφεύγεται το μήνυμα «JavaFX runtime components are missing».)
5. Tests: δεξί κλικ στο `src/test/java` → **Run 'All Tests'**.

## 4. Screenshots για το Παραδοτέο 2

Η εφαρμογή έχει ένα σενάριο που πατά τα πραγματικά κουμπιά της διεπαφής με δοκιμαστικά δεδομένα και σώζει
26 εικόνες με ονόματα όπως `SS-UC18-02_mi_egkyri_ypografi.png`:

```bash
mvn javafx:run -Dapp.args=--screenshots=docs/screenshots
```

Δουλεύει σε προσωρινό φάκελο δεδομένων, άρα δεν πειράζει τα δικά σας θησαυροφυλάκια ή το ιστορικό. Οι έτοιμες
εικόνες βρίσκονται ήδη στο [`docs/screenshots`](docs/screenshots). Το ίδιο σενάριο λειτουργεί και ως αυτόματος έλεγχος
της διεπαφής: τελειώνει με `SCREENSHOTS OK: 26` μόνο αν πέτυχαν όλα τα βήματα.

## 5. Μετρικές ποιότητας (MetricsCalculator.jar)

Το jar του μαθήματος αποτυγχάνει όταν η διαδρομή έχει ελληνικούς χαρακτήρες. Γι' αυτό:

1. Αντιγράψτε το `MetricsCalculator.jar` στο `C:\ml-tools\`.
2. Αντιγράψτε τον φάκελο `src\main` του project στο `C:\ml-tools\reference-app\src\main`
   (μόνο ο κώδικας εφαρμογής, όχι τα tests).
3. Από το `C:\ml-tools`:

```bash
java -jar MetricsCalculator.jar C:/ml-tools/reference-app C:/ml-tools/metrics.csv
```

Το αποτέλεσμα για αυτή την έκδοση είναι στο [`docs/metrics.csv`](docs/metrics.csv) (διαχωριστικό `;`).
Σύνοψη και ερμηνεία στο SXEDIASH.md, ενότητα «Μετρικές».

Περιορισμοί του εργαλείου που επηρεάζουν τον κώδικα (βλ. και SXEDIASH.md):
- Δίνει 0 σε κλάσεις με νέα σύνταξη (`record`, `switch` με `->`, text blocks, pattern matching στο `instanceof`): δεν χρησιμοποιούνται.
- Δίνει 0 σε κλάσεις που έχουν **μέθοδο με παράμετρο τύπου JavaFX** (δεν βρίσκει τις κλάσεις του JavaFX).
  Γι' αυτό οι τύποι JavaFX περνούν μόνο σε κατασκευαστές (π.χ. `DropZone`, `StrengthIndicator`, `ThemeManager`).
- Τα `enum` και η `App` (κληρονομεί από `javafx.application.Application`) βγαίνουν με μηδενικές τιμές.

## 6. Πού αποθηκεύει δεδομένα η εφαρμογή

Στον φάκελο `%USERPROFILE%\.omada12-crypto\` (ίδια λογική σε Linux/macOS: `~/.omada12-crypto/`):

| Αρχείο | Περιεχόμενο |
|---|---|
| `settings.properties` | γλώσσα, θέμα, χρώμα, χρόνος αυτόματου κλειδώματος, ιστορικό on/off |
| `history.log` | ιστορικό ενεργειών (ημερομηνία, ενότητα, ενέργεια, όνομα αρχείου, αποτέλεσμα) |
| `vaults\*.o12vault` | τα θησαυροφυλάκια (ένα κρυπτογραφημένο αρχείο το καθένα) |

Με `-Domada12.home=ΦΑΚΕΛΟΣ` ο φάκελος αλλάζει (χρήσιμο για δοκιμές).

Μορφές αρχείων που δημιουργεί η εφαρμογή: `.enc` (κρυπτογραφημένο αρχείο), `.o12vault` (θησαυροφυλάκιο),
`.o12key` (ιδιωτικό κλειδί υπογραφής, κρυπτογραφημένο), `.pub` (δημόσιο κλειδί PEM), `.sig` (υπογραφή, κείμενο),
`.key` (τυχαίο κλειδί AES σε Base64). Λεπτομέρειες στο SXEDIASH.md.

## 7. Δομή πακέτων

```
gr.ihu.omada12.crypto
├── App, AppStartup, Launcher        εκκίνηση (το App είναι σκόπιμα μικρό)
├── model                            ΜΟΝΤΕΛΟ: καθαρή Java, χωρίς JavaFX, όλα με JUnit tests
│   ├── cipher      TextCipher (Strategy), CaesarCipher, VigenereCipher, AesCipher, AesKeyCipher,
│   │               CipherFactory (Factory), CryptoException (κλειδιά μηνυμάτων)
│   ├── crypto      AesGcm, KeyDerivation (PBKDF2), PasswordBox (κλείδωμα bytes με κωδικό), Hex
│   ├── key         KeyManager (τυχαίο κλειδί AES σε αρχείο)
│   ├── io          AtomicFileWriter (προσωρινό αρχείο + μετονομασία), FileProcessor, ProgressListener
│   ├── file        FileCryptoService (.enc), EncFileHeader, StreamCipherPump, BatchEncryptor, PdfLocker
│   ├── vault       Vault, VaultStore (δοχείο), VaultCodec, VaultEntry, VaultManager, VaultFilter,
│   │               VaultSession, InactivityTimer, LoginThrottle, VaultInfo, PreviewKind
│   ├── signature   SignatureAlgorithm (Ed25519), PrivateKeyStore, PublicKeyPem, FileSigner,
│   │               SignatureVerifier, SignatureRecord, SignatureFileCodec, VerificationResult, FileHasher
│   ├── stego       StegoService, LsbCodec, StegoFrame, PayloadKind, HiddenFile, RevealedPayload, ImageFormatPolicy
│   ├── lab         BenchmarkAlgorithm (Strategy) + AesGcm/AesCbc/ChaCha20Benchmark, BenchmarkCatalog,
│   │               BenchmarkRunner, BenchmarkResult, BenchmarkCsv
│   ├── analysis    BruteForceAnalyzer, FrequencyAnalyzer, Candidate, CipherStepper, CipherStep
│   ├── password    PasswordStrengthMeter, PasswordStrength, CrackTime, PasswordGenerator, PasswordPolicy
│   ├── history     HistoryService, HistoryRepository, HistoryEntry, HistoryFilter, AppModule
│   └── settings    Settings, SettingsRepository, AppPaths
├── controller                       CONTROLLERS: ένας ανά FXML (μικροί, μόνο «συνδέουν» διεπαφή και μοντέλο)
│   ├── HomeController
│   ├── encryption  TextCryptoController, FileCryptoController, PdfController
│   ├── vault       VaultListController, VaultController, VaultEntryActions
│   ├── signature   SigKeysController, SignController, VerifyController, HashController
│   ├── stego       StegoHideController, StegoRevealController
│   ├── lab         BenchmarkController, CaesarLabController, PasswordLabController, StepperController
│   └── general     SettingsController, HistoryController
├── view                             ΥΠΟΔΟΜΗ ΔΙΕΠΑΦΗΣ: Navigator, Screen, AppContext, ControllerFactory, I18n,
│                                    ThemeManager, ActionRunner, TaskRunner, Feedback, Dialogs, FileDialogs,
│                                    FileSelection, FileFilter, DropZone, StrengthIndicator, ChoiceFilter,
│                                    PreviewWindow, ChangePasswordDialog, TutorialDialog, AutoLockMonitor, ExitGuard, …
└── tools                            εργαλείο screenshots: ScreenshotTool, UiRobot, DemoData

src/main/resources/gr/ihu/omada12/crypto
├── view/*.fxml        23 αρχεία FXML (8 οθόνες UI-01…UI-08, οι 4 με καρτέλες)
├── css/               base.css, light.css, dark.css
└── i18n/              messages_el.properties, messages_en.properties
```

Οθόνες: **UI-01** Αρχική · **UI-02** Κρυπτογράφηση (Κείμενο/Αρχεία/PDF) · **UI-03** Λίστα θησαυροφυλακίων ·
**UI-04** Περιεχόμενα θησαυροφυλακίου · **UI-05** Υπογραφή (Κλειδιά/Υπογραφή/Επαλήθευση/SHA-256) ·
**UI-06** Στεγανογραφία (Απόκρυψη/Ανάκτηση) · **UI-07** Εργαστήριο (Σύγκριση/Caesar/Κωδικοί/Βήμα-βήμα) ·
**UI-08** Γενικά (Ρυθμίσεις/Ιστορικό).

## 8. Πίνακας «ID λειτουργίας → κλάση → test»

Οι κλάσεις του μοντέλου είναι στο `model.*`, οι controllers στο `controller.*`, τα tests στο `src/test/java` με το
ίδιο πακέτο. «Χειροκίνητα / screenshot» σημαίνει ότι η λειτουργία είναι καθαρά διεπαφής: ελέγχεται από το σενάριο
screenshots (`tools.ScreenshotTool`) ή με το χέρι.

### Α. Κρυπτογράφηση

| ID | Προτ. | Κλάσεις | Test |
|---|---|---|---|
| ENC-01 | M | `TextCipher`, `CipherFactory`, `CaesarCipher`, `VigenereCipher`, `AesCipher`, `AesKeyCipher`, `TextCryptoController` | `CipherTest`, `AesCipherTest`, `CipherFactoryTest`, `OfficialTestCasesTest.tc01` |
| ENC-02 | M | ίδιες με ENC-01, `Base64Text` | `CipherTest.caesarDecryptReversesEncrypt`, `AesCipherTest.passwordRoundTripWithGreekText`, `.invalidBase64IsReported` |
| ENC-03 | M | `KeyDerivation`, `PasswordBox`, `AesCipher` | `KeyDerivationTest`, `AesCipherTest.wrongPasswordFailsInsteadOfReturningGarbage` |
| ENC-04 | M | `FileCryptoService`, `EncFileHeader`, `StreamCipherPump`, `FileCryptoController` | `FileCryptoServiceTest.encryptThenDecryptGivesIdenticalFile` |
| ENC-05 | M | `FileCryptoService`, `AtomicFileWriter` | `FileCryptoServiceTest.wrongPasswordCreatesNoOutputFile`, `.tamperedFileIsDetected`, `OfficialTestCasesTest.tc02` |
| ENC-06 | S | `DropZone`, `FileSelection`, `FileDialogs` | χειροκίνητα / screenshot `SS-UC03-01` |
| ENC-07 | S | `PdfLocker`, `PdfController` | `PdfLockerTest` |
| ENC-08 | S | `KeyManager`, `AesKeyCipher` | `KeyManagerTest`, `CipherTest.aesRejectsWrongKey` |
| ENC-09 | S | `Views.copyToClipboard`, `TextCryptoController.saveOutput` | χειροκίνητα |
| ENC-10 | S | `ProgressListener`, `StreamCipherPump`, `TaskRunner`, `ActionRunner` | `FileCryptoServiceTest.cancelLeavesNoPartialOutput` |
| ENC-11 | C | `BatchEncryptor`, `BatchReport` | `BatchEncryptorTest` |

### Β. Secure Vault

| ID | Προτ. | Κλάσεις | Test |
|---|---|---|---|
| VLT-01 | M | `VaultManager.create/checkNewPassword`, `VaultStore`, `PasswordStrengthMeter`, `StrengthIndicator`, `VaultListController` | `VaultManagerTest.createValidatesConfirmationAndStrength` |
| VLT-02 | M | `VaultManager.unlock`, `VaultStore.open` | `VaultTest.lockThenUnlockShowsSameEntries`, `.wrongMasterPasswordIsRejected` |
| VLT-03 | M | `Vault.addFile`, `VaultCodec`, `VaultStore.save`, `DropZone` | `VaultTest.containerRevealsNeitherNamesNorContent` |
| VLT-04 | M | `VaultEntry`, `VaultController` | `VaultTest.lockThenUnlockShowsSameEntries` |
| VLT-05 | M | `VaultFilter`, `ChoiceFilter` | `VaultFilterTest.filtersByNameTypeTagAndDate` |
| VLT-06 | M | `Vault.export/exportTarget`, `VaultEntryActions.export` | `VaultTest.exportAndPreviewGiveOriginalBytes` |
| VLT-07 | M | `Vault.lock`, `VaultSession.lock`, `VaultController.lock` | `VaultTest.lockThenUnlockShowsSameEntries` |
| VLT-08 | S | `InactivityTimer`, `VaultSession.lockIfIdle`, `AutoLockMonitor`, `ExitGuard`, `AppStartup.stop` | `VaultTimingTest.sessionLocksAfterIdleTimeout` |
| VLT-09 | S | `Vault.delete`, `VaultEntryActions.delete`, `Dialogs.confirm` | `VaultTest.deleteAndTagArePersisted` |
| VLT-10 | S | `Vault.changePassword`, `ChangePasswordDialog` | `VaultTest.changePasswordReplacesOldOne` |
| VLT-11 | S | `VaultManager` (list/rename/delete), `VaultInfo`, `VaultListController` | `VaultManagerTest.listRenameDelete` |
| VLT-12 | S | `LoginThrottle`, `VaultManager.unlock` | `VaultManagerTest.threeWrongPasswordsTriggerDelay`, `VaultTimingTest`, `OfficialTestCasesTest.tc03` |
| VLT-13 | C | `VaultEntry.tag`, `Vault.setTag`, `VaultFilter.tag` | `VaultTest.deleteAndTagArePersisted`, `VaultFilterTest` |
| VLT-14 | C | `Vault.read`, `PreviewKind`, `PreviewWindow` | `VaultTest.exportAndPreviewGiveOriginalBytes`, `VaultFilterTest.previewKindFromType` |

### Γ. Ψηφιακή υπογραφή

| ID | Προτ. | Κλάσεις | Test |
|---|---|---|---|
| SIG-01 | M | `SignatureAlgorithm` (Ed25519), `PrivateKeyStore`, `SigKeysController` | `SignatureTest.privateKeyIsProtectedByPassword` |
| SIG-02 | M | `FileSigner`, `SignatureRecord`, `SignatureFileCodec`, `SignController` | `SignatureTest.signedFileVerifiesAndShowsDetails` |
| SIG-03 | M | `SignatureVerifier`, `VerificationResult`, `VerifyController` | `SignatureTest.oneChangedByteInvalidatesSignature`, `.otherPublicKeyIsRejected`, `OfficialTestCasesTest.tc04` |
| SIG-04 | S | `PublicKeyPem` | `SignatureTest.exportedPublicKeyVerifiesSignatures` |
| SIG-05 | S | `FileHasher`, `Hex`, `HashController` | `SignatureTest.sha256MatchesKnownValueAndComparesFiles` |
| SIG-06 | C | `SignatureRecord`, `SignatureAlgorithm.fingerprint`, `VerifyController` | `SignatureTest.signedFileVerifiesAndShowsDetails` |

### Δ. Στεγανογραφία

| ID | Προτ. | Κλάσεις | Test |
|---|---|---|---|
| STG-01 | M | `StegoService.hideText`, `LsbCodec`, `StegoFrame` | `StegoServiceTest.hiddenTextIsRecoveredAndImageLooksTheSame`, `OfficialTestCasesTest.tc05` |
| STG-02 | M | `StegoService.reveal`, `RevealedPayload`, `StegoRevealController` | ίδια με STG-01 |
| STG-03 | S | `PasswordBox`, `PayloadKind` | `StegoServiceTest.encryptedMessageNeedsTheRightPassword` |
| STG-04 | S | `StegoService.freeBytes`, `LsbCodec.capacityBytes`, `StegoHideController` | `StegoServiceTest.capacityIsReportedAndEnforced` |
| STG-05 | C | `StegoHideController` (δύο `ImageView`) | χειροκίνητα / screenshot `SS-UC20-01` |
| STG-06 | C | `HiddenFile`, `StegoService.hideFile` | `StegoServiceTest.smallFileCanBeHidden` |

### Ε. Εργαστήριο

| ID | Προτ. | Κλάσεις | Test |
|---|---|---|---|
| LAB-01 | M | `BenchmarkAlgorithm`, `JceBenchmarkAlgorithm`, `AesGcmBenchmark`, `AesCbcBenchmark`, `ChaCha20Benchmark`, `BenchmarkCatalog`, `BenchmarkRunner` | `BenchmarkTest.comparesThreeAlgorithmsWithSensibleNumbers` |
| LAB-02 | S | `BenchmarkRunner` (Ν επαναλήψεις + ζέσταμα) | `BenchmarkTest` |
| LAB-03 | S | `BenchmarkController` (`BarChart`) | screenshot `SS-UC22-01` |
| LAB-04 | C | `BenchmarkCsv` | `BenchmarkTest.csvHasHeaderAndOneLinePerAlgorithm` |
| LAB-05 | M | `BruteForceAnalyzer`, `Candidate`, `CaesarLabController` | `AnalysisTest.bruteForceFindsShiftThree`, `.bruteForceWorksForAllShifts` |
| LAB-06 | C | `FrequencyAnalyzer`, `CaesarLabController` | `AnalysisTest.frequenciesSumToHundred` |
| LAB-07 | S | `PasswordStrengthMeter`, `PasswordStrength`, `CrackTime` | `PasswordTest` |
| LAB-08 | S | `PasswordGenerator`, `PasswordPolicy` | `PasswordTest.generatorRespectsPolicy`, `.generatorRejectsBadPolicy` |
| LAB-09 | C | `CipherStepper`, `CipherStep`, `StepperController` | `AnalysisTest.stepsMatchRealCiphers` |

### ΣΤ. Γενικές λειτουργίες

| ID | Προτ. | Κλάσεις | Test |
|---|---|---|---|
| APP-01 | M | `HomeController`, `home.fxml`, `Screen`, `AppModule` | screenshot `SS-UI01` |
| APP-02 | M | `Navigator` (Πίσω / Αρχική σε κάθε οθόνη) | χειροκίνητα |
| APP-03 | M | `ExitGuard`, `AppStartup.stop`, `VaultSession.lock` | `VaultTimingTest` + χειροκίνητα |
| APP-04 | M | `CryptoException`, `I18n`, `Feedback`, `messages_*.properties` | `BundleConsistencyTest` |
| APP-05 | M | `Dialogs.confirm` (διαγραφή, αντικατάσταση, καθαρισμός ιστορικού) | χειροκίνητα |
| APP-06 | S | `I18n`, `SettingsController`, `messages_el/en` | `BundleConsistencyTest.greekAndEnglishHaveTheSameKeys` |
| APP-07 | S | `ThemeManager`, `base/light/dark.css` | `SettingsRepositoryTest`, screenshot `SS-UC25-02` |
| APP-08 | S | `SettingsRepository`, `Settings` | `SettingsRepositoryTest.settingsSurviveRestart` |
| APP-09 | S | `HistoryService`, `HistoryRepository`, `HistoryEntry` | `HistoryServiceTest` |
| APP-10 | S | `HistoryFilter`, `HistoryService.setEnabled/clear`, `HistoryController` | `HistoryServiceTest.disabledHistoryWritesNothingAndClearRemovesAll` |
| APP-11 | S | `Navigator.showHelp`, `Dialogs.help`, κλειδιά `help.*` | `BundleConsistencyTest.dynamicKeysExist` |
| APP-12 | S | `TutorialDialog`, `AppStartup.firstRunTutorial` | `BundleConsistencyTest.dynamicKeysExist` + χειροκίνητα |

### Μη λειτουργικές απαιτήσεις

| ID | Πώς ικανοποιείται | Test |
|---|---|---|
| NFR-01 | `AesGcm`: AES/GCM/NoPadding, νέο τυχαίο IV 12 bytes κάθε φορά | `AesCipherTest.sameInputGivesDifferentCipherTexts` |
| NFR-02 | `KeyDerivation`: PBKDF2WithHmacSHA256, 600.000, salt 16 bytes | `KeyDerivationTest.usesOwaspParameters` |
| NFR-03 | `HistoryEntry` δεν έχει πεδίο για κωδικό/περιεχόμενο· τα κλειδιά γράφονται μόνο σε αρχείο που διαλέγει ο χρήστης | `HistoryServiceTest.fileContainsOnlyMetadata` |
| NFR-04 | Κανένας μηχανισμός ανάκτησης· προειδοποίηση + υποχρεωτικό checkbox στη δημιουργία (`vault_list.fxml`) | screenshot `SS-UC07-01` |
| NFR-05 | `AtomicFileWriter`: προσωρινό `.part` στον ίδιο φάκελο, μετονομασία στο τέλος | `FileCryptoServiceTest.cancelLeavesNoPartialOutput` |
| NFR-06 | Κείμενο 10.000 χαρ. < 1 s, αρχείο 50 MB < 5 s | `AesCipherTest.tenThousandCharactersUnderOneSecond`, `FileCryptoServiceTest.fiftyMegabytesUnderFiveSeconds` |
| NFR-07 | Αρχική → ενότητα → ενέργεια (καρτέλες, όχι υπο-μενού) | χειροκίνητα |
| NFR-08 | `ImageFormatPolicy`: PNG/BMP, JPEG με προειδοποίηση, έξοδος πάντα PNG | `StegoServiceTest.outputIsAlwaysPng` |
| NFR-09 | Όλα τα κείμενα σε `messages_el/en.properties`· το μοντέλο στέλνει μόνο κλειδιά | `BundleConsistencyTest` |
| NFR-10 | Καθαρή Java 17 + JavaFX από Maven, `AppPaths` με `user.home` | `mvn clean test` |
| NFR-11 | Νέος αλγόριθμος = νέα κλάση `TextCipher` (ή `BenchmarkAlgorithm`) + 1 γραμμή στο Factory | `CipherFactoryTest` |
| NFR-12 | Κανένας κώδικας δικτύου, απλά αρχεία αντί για βάση δεδομένων | έλεγχος κώδικα |
