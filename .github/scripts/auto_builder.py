import os, shutil, sys, subprocess
from datetime import datetime

WEEK = sys.argv[1] # π.χ. E3, E4, E5...
BASE = "reference-app" # Ο φάκελος βάσης στο αρχικό repo
TARGET = "target-repo" # Το κλωνοποιημένο omada12-crypto

def copy_file(src_rel, dst_rel):
    src = os.path.join(BASE, src_rel)
    dst = os.path.join(TARGET, dst_rel)
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    if os.path.exists(src):
        shutil.copy2(src, dst)

def git_push(role_name, role_email, token, branch, commit_msg):
    repo_url = f"https://{token}@github.com/petros137/omada12-crypto.git"
    subprocess.run(["git", "checkout", "main"], cwd=TARGET, check=True)
    subprocess.run(["git", "pull", repo_url, "main"], cwd=TARGET, check=True)
    subprocess.run(["git", "checkout", "-B", branch], cwd=TARGET, check=True)
    subprocess.run(["git", "config", "user.name", role_name], cwd=TARGET, check=True)
    subprocess.run(["git", "config", "user.email", role_email], cwd=TARGET, check=True)
    subprocess.run(["git", "add", "."], cwd=TARGET, check=True)
    # Έλεγχος αν υπάρχουν αλλαγές
    status = subprocess.run(["git", "status", "--porcelain"], cwd=TARGET, capture_output=True, text=True)
    if status.stdout.strip():
        subprocess.run(["git", "commit", "-m", commit_msg], cwd=TARGET, check=True)
        subprocess.run(["git", "push", "-u", repo_url, branch, "--force"], cwd=TARGET, check=True)

# Στοιχεία Μελών (Αλλάξτε τα emails και τα usernames με τα πραγματικά σας)
ILIAS = ("Ilias Gavriilidis", "ilias@example.com", os.environ["TOKEN_ILIAS"])
LEONIDAS = ("Leonidas Dimitriadis", "leonidas@example.com", os.environ["TOKEN_LEONIDAS"])
APOSTOLOS = ("Apostolos Dagklis", "apostolos@example.com", os.environ["TOKEN_APOSTOLOS"])
NIKOS = ("Nikos Petrakis", "nikos@example.com", os.environ["TOKEN_NIKOS"])

J_BASE = "src/main/java/gr/ihu/omada12/crypto"
R_BASE = "src/main/resources/gr/ihu/omada12/crypto"
T_BASE = "src/test/java/gr/ihu/omada12/crypto"

# ==========================================
# ΕΒΔΟΜΑΔΑ Ε3 (26/10 - 1/11)
# ==========================================
if WEEK == "E3":
    # 1. ΗΛΙΑΣ: pom.xml, .gitignore + 7 κλάσεις cipher (ΧΩΡΙΣ AES στο CipherFactory)
    copy_file("pom.xml", "app/pom.xml")
    copy_file(".gitignore", "app/.gitignore")
    for f in ["TextCipher", "ClassicalCipher", "CaesarCipher", "VigenereCipher", "CipherFactory", "CryptoException", "Base64Text"]:
        copy_file(f"{J_BASE}/model/cipher/{f}.java", f"app/{J_BASE}/model/cipher/{f}.java")
    
    # Εφαρμογή οδηγίας PDF Ηλία Ε3: Σβήσιμο AES από το CipherFactory
    cf_path = os.path.join(TARGET, f"app/{J_BASE}/model/cipher/CipherFactory.java")
    with open(cf_path, "r", encoding="utf-8") as file:
        content = file.read()
    content = content.replace("AES, AES_KEY, ", "").replace("case AES: return new AesCipher();\n", "").replace("case AES_KEY: return new AesKeyCipher();\n", "")
    with open(cf_path, "w", encoding="utf-8") as file:
        file.write(content)
    git_push(*ILIAS, "feat/cipher-classical", "ENC-01/02: Caesar και Vigenère (Strategy), CipherFactory (Factory)")

    # 2. ΑΠΟΣΤΟΛΟΣ: CipherTest (ΧΩΡΙΣ aesRejectsWrongKey)
    copy_file(f"{T_BASE}/model/cipher/CipherTest.java", f"app/{T_BASE}/model/cipher/CipherTest.java")
    ct_path = os.path.join(TARGET, f"app/{T_BASE}/model/cipher/CipherTest.java")
    with open(ct_path, "r", encoding="utf-8") as file:
        lines = file.readlines()
    # Αφαίρεση του test aesRejectsWrongKey για την Ε3
    filtered = [l for l in lines if "KeyManager" not in l and "AesKeyCipher" not in l and "aesRejectsWrongKey" not in l and "μυστικό μήνυμα" not in l]
    with open(ct_path, "w", encoding="utf-8") as file:
        file.writelines(filtered)
    git_push(*APOSTOLOS, "test/cipher", "TC-01/02/05: JUnit για Caesar και Vigenère")

# ==========================================
# ΕΒΔΟΜΑΔΑ Ε4 (2/11 - 8/11)
# ==========================================
elif WEEK == "E4":
    # 1. ΗΛΙΑΣ: AES-GCM, PBKDF2, Κλειδιά, IO + Επαναφορά πλήρους CipherFactory
    for f in ["AesGcm", "Hex", "KeyDerivation", "PasswordBox"]:
        copy_file(f"{J_BASE}/model/crypto/{f}.java", f"app/{J_BASE}/model/crypto/{f}.java")
    for f in ["AesCipher", "AesKeyCipher", "CipherFactory"]:
        copy_file(f"{J_BASE}/model/cipher/{f}.java", f"app/{J_BASE}/model/cipher/{f}.java")
    copy_file(f"{J_BASE}/model/key/KeyManager.java", f"app/{J_BASE}/model/key/KeyManager.java")
    for f in ["AtomicFileWriter", "FileProcessor", "ProgressListener"]:
        copy_file(f"{J_BASE}/model/io/{f}.java", f"app/{J_BASE}/model/io/{f}.java")
    git_push(*ILIAS, "feat/aes", "NFR-02: PBKDF2, NFR-01: AES-GCM, ENC-03: AES με κωδικό, ENC-08: κλειδιά σε αρχείο")

    # 2. ΑΠΟΣΤΟΛΟΣ: Tests AES + Πλήρες CipherTest
    for f in ["CipherTest", "AesCipherTest", "CipherFactoryTest"]:
        copy_file(f"{T_BASE}/model/cipher/{f}.java", f"app/{T_BASE}/model/cipher/{f}.java")
    copy_file(f"{T_BASE}/model/crypto/KeyDerivationTest.java", f"app/{T_BASE}/model/crypto/KeyDerivationTest.java")
    copy_file(f"{T_BASE}/model/key/KeyManagerTest.java", f"app/{T_BASE}/model/key/KeyManagerTest.java")
    git_push(*APOSTOLOS, "test/crypto", "TC-04/07/08: JUnit tests για AES, PBKDF2 και KeyManager")

# ==========================================
# ΕΒΔΟΜΑΔΑ Ε5 (9/11 - 15/11)
# ==========================================
elif WEEK == "E5":
    # 1. ΗΛΙΑΣ: Υποδομή MVC + Ιστορικό + Ρυθμίσεις + TextCryptoController
    for f in ["App", "AppStartup", "Launcher"]:
        copy_file(f"{J_BASE}/{f}.java", f"app/{J_BASE}/{f}.java")
    for f in ["Screen", "Navigator", "AppContext", "ControllerFactory", "I18n", "ThemeManager", "Feedback", "Dialogs", "FileDialogs", "FileFilter", "Formats", "TaskRunner", "ActionRunner", "ExitGuard", "Views"]:
        copy_file(f"{J_BASE}/view/{f}.java", f"app/{J_BASE}/view/{f}.java")
    for f in ["AppModule", "HistoryEntry", "HistoryFilter", "HistoryRepository", "HistoryService"]:
        copy_file(f"{J_BASE}/model/history/{f}.java", f"app/{J_BASE}/model/history/{f}.java")
    for f in ["AppPaths", "Settings", "SettingsRepository"]:
        copy_file(f"{J_BASE}/model/settings/{f}.java", f"app/{J_BASE}/model/settings/{f}.java")
    copy_file(f"{J_BASE}/controller/HomeController.java", f"app/{J_BASE}/controller/HomeController.java")
    copy_file(f"{J_BASE}/controller/encryption/TextCryptoController.java", f"app/{J_BASE}/controller/encryption/TextCryptoController.java")
    git_push(*ILIAS, "feat/mvc-text", "APP-01: αρχική οθόνη, APP-02: πλοήγηση, APP-09: ιστορικό, UC-01: κρυπτογράφηση κειμένου")

    # 2. ΛΕΩΝΙΔΑΣ: CSS + Αρχική + Κρυπτογράφηση κειμένου FXML + Bundles
    for f in ["base.css", "light.css", "dark.css"]:
        copy_file(f"{R_BASE}/css/{f}", f"app/{R_BASE}/css/{f}")
    for f in ["home.fxml", "encryption.fxml", "enc_text.fxml"]:
        copy_file(f"{R_BASE}/view/{f}", f"app/{R_BASE}/view/{f}")
    for f in ["messages_el.properties", "messages_en.properties"]:
        copy_file(f"{R_BASE}/i18n/{f}", f"app/{R_BASE}/i18n/{f}")
    git_push(*LEONIDAS, "ui/home-encryption", "UI-01: αρχική οθόνη, UI-02: καρτέλα κειμένου, CSS: φωτεινό/σκοτεινό θέμα")

    # 3. ΑΠΟΣΤΟΛΟΣ: Tests Ιστορικού & Ρυθμίσεων
    copy_file(f"{T_BASE}/model/history/HistoryServiceTest.java", f"app/{T_BASE}/model/history/HistoryServiceTest.java")
    copy_file(f"{T_BASE}/model/settings/SettingsRepositoryTest.java", f"app/{T_BASE}/model/settings/SettingsRepositoryTest.java")
    copy_file(f"{T_BASE}/view/BundleConsistencyTest.java", f"app/{T_BASE}/view/BundleConsistencyTest.java")
    git_push(*APOSTOLOS, "test/history-settings", "APP-06/08/09: Tests ιστορικού, ρυθμίσεων και συνέπειας bundles")

# ==========================================
# ΕΒΔΟΜΑΔΑ Ε6 (16/11 - 22/11)
# ==========================================
elif WEEK == "E6":
    # 1. ΗΛΙΑΣ: Αρχεία .enc και PDF
    for f in ["BatchEncryptor", "BatchReport", "EncFileHeader", "FileCryptoService", "PdfLocker", "StreamCipherPump"]:
        copy_file(f"{J_BASE}/model/file/{f}.java", f"app/{J_BASE}/model/file/{f}.java")
    for f in ["FileSelection", "DropZone"]:
        copy_file(f"{J_BASE}/view/{f}.java", f"app/{J_BASE}/view/{f}.java")
    for f in ["FileCryptoController", "PdfController"]:
        copy_file(f"{J_BASE}/controller/encryption/{f}.java", f"app/{J_BASE}/controller/encryption/{f}.java")
    git_push(*ILIAS, "feat/files", "ENC-04/05: αρχεία .enc, ENC-10: πρόοδος και ακύρωση, ENC-11: μαζική κρυπτογράφηση, ENC-07: PDF")

    # 2. ΛΕΩΝΙΔΑΣ: FXML Αρχείων και PDF
    for f in ["enc_file.fxml", "enc_pdf.fxml", "encryption.fxml"]:
        copy_file(f"{R_BASE}/view/{f}", f"app/{R_BASE}/view/{f}")
    git_push(*LEONIDAS, "ui/files-pdf", "UI-02: FXML καρτέλες Αρχείων και PDF")

    # 3. ΑΠΟΣΤΟΛΟΣ: Tests Αρχείων και PDF
    for f in ["BatchEncryptorTest", "FileCryptoServiceTest", "PdfLockerTest"]:
        copy_file(f"{T_BASE}/model/file/{f}.java", f"app/{T_BASE}/model/file/{f}.java")
    copy_file(f"{T_BASE}/OfficialTestCasesTest.java", f"app/{T_BASE}/OfficialTestCasesTest.java")
    git_push(*APOSTOLOS, "test/files", "ENC-04/05/07/11: JUnit tests αρχείων, PDF και TC-01/TC-02")

# ==========================================
# ΕΒΔΟΜΑΔΑ Ε7 (23/11 - 29/11)
# ==========================================
elif WEEK == "E7":
    # 1. ΗΛΙΑΣ: Secure Vault & Password
    for f in ["CrackTime", "PasswordGenerator", "PasswordPolicy", "PasswordStrength", "PasswordStrengthMeter"]:
        copy_file(f"{J_BASE}/model/password/{f}.java", f"app/{J_BASE}/model/password/{f}.java")
    for f in ["InactivityTimer", "LoginThrottle", "PreviewKind", "Vault", "VaultCodec", "VaultEntry", "VaultFilter", "VaultInfo", "VaultManager", "VaultSession", "VaultStore"]:
        copy_file(f"{J_BASE}/model/vault/{f}.java", f"app/{J_BASE}/model/vault/{f}.java")
    for f in ["StrengthIndicator", "ChangePasswordDialog", "ChoiceFilter", "PreviewWindow", "AutoLockMonitor", "AppContext", "ExitGuard", "Formats", "Screen"]:
        copy_file(f"{J_BASE}/view/{f}.java", f"app/{J_BASE}/view/{f}.java")
    for f in ["VaultController", "VaultEntryActions", "VaultListController"]:
        copy_file(f"{J_BASE}/controller/vault/{f}.java", f"app/{J_BASE}/controller/vault/{f}.java")
    git_push(*ILIAS, "feat/vault", "VLT-01/02: δημιουργία και ξεκλείδωμα, VLT-03...06: περιεχόμενα, VLT-07/08: κλείδωμα, VLT-12: καθυστέρηση")

    # 2. ΛΕΩΝΙΔΑΣ: FXML Θησαυροφυλακίου
    for f in ["vault_list.fxml", "vault.fxml", "home.fxml"]:
        copy_file(f"{R_BASE}/view/{f}", f"app/{R_BASE}/view/{f}")
    git_push(*LEONIDAS, "ui/vault", "UI-03/UI-04: Οθόνες λίστας και περιεχομένων Secure Vault")

    # 3. ΑΠΟΣΤΟΛΟΣ: Tests Vault & Password
    for f in ["MutableClock", "VaultFilterTest", "VaultManagerTest", "VaultTest", "VaultTimingTest"]:
        copy_file(f"{T_BASE}/model/vault/{f}.java", f"app/{T_BASE}/model/vault/{f}.java")
    copy_file(f"{T_BASE}/model/password/PasswordTest.java", f"app/{T_BASE}/model/password/PasswordTest.java")
    git_push(*APOSTOLOS, "test/vault", "VLT-01...14: JUnit tests Secure Vault, Password και TC-03")

# ==========================================
# ΕΒΔΟΜΑΔΑ Ε8 (30/11 - 6/12)
# ==========================================
elif WEEK == "E8":
    # 1. ΗΛΙΑΣ: Υπογραφή & Στεγανογραφία
    for f in ["FileHasher", "FileSigner", "PrivateKeyStore", "PublicKeyPem", "SignatureAlgorithm", "SignatureFileCodec", "SignatureRecord", "SignatureVerifier", "VerificationResult"]:
        copy_file(f"{J_BASE}/model/signature/{f}.java", f"app/{J_BASE}/model/signature/{f}.java")
    for f in ["HashController", "SigKeysController", "SignController", "VerifyController"]:
        copy_file(f"{J_BASE}/controller/signature/{f}.java", f"app/{J_BASE}/controller/signature/{f}.java")
    git_push(*ILIAS, "feat/signature", "SIG-01...06: Ψηφιακή υπογραφή Ed25519 και αποτυπώματα SHA-256")

    for f in ["HiddenFile", "ImageFormatPolicy", "LsbCodec", "PayloadKind", "RevealedPayload", "StegoFrame", "StegoService"]:
        copy_file(f"{J_BASE}/model/stego/{f}.java", f"app/{J_BASE}/model/stego/{f}.java")
    for f in ["StegoHideController", "StegoRevealController"]:
        copy_file(f"{J_BASE}/controller/stego/{f}.java", f"app/{J_BASE}/controller/stego/{f}.java")
    git_push(*ILIAS, "feat/stego", "STG-01...06: Στεγανογραφία LSB σε PNG")

    # 2. ΛΕΩΝΙΔΑΣ: FXML Υπογραφής & Στεγανογραφίας
    for f in ["signature.fxml", "sig_keys.fxml", "sig_sign.fxml", "sig_verify.fxml", "sig_hash.fxml", "stego.fxml", "stego_hide.fxml", "stego_reveal.fxml"]:
        copy_file(f"{R_BASE}/view/{f}", f"app/{R_BASE}/view/{f}")
    git_push(*LEONIDAS, "ui/signature-stego", "UI-05/UI-06: Οθόνες ψηφιακής υπογραφής και στεγανογραφίας")

    # 3. ΑΠΟΣΤΟΛΟΣ: Tests Υπογραφής & Στεγανογραφίας
    copy_file(f"{T_BASE}/model/signature/SignatureTest.java", f"app/{T_BASE}/model/signature/SignatureTest.java")
    copy_file(f"{T_BASE}/model/stego/StegoServiceTest.java", f"app/{T_BASE}/model/stego/StegoServiceTest.java")
    git_push(*APOSTOLOS, "test/signature-stego", "SIG/STG: JUnit tests υπογραφής, στεγανογραφίας και TC-04/TC-05")

# ==========================================
# ΕΒΔΟΜΑΔΑ Ε9 (7/12 - 13/12)
# ==========================================
elif WEEK == "E9":
    # 1. ΗΛΙΑΣ: Εργαστήριο & Γενικά -> Code Freeze
    for f in ["BruteForceAnalyzer", "Candidate", "CipherStep", "CipherStepper", "FrequencyAnalyzer"]:
        copy_file(f"{J_BASE}/model/analysis/{f}.java", f"app/{J_BASE}/model/analysis/{f}.java")
    for f in ["AesCbcBenchmark", "AesGcmBenchmark", "BenchmarkAlgorithm", "BenchmarkCatalog", "BenchmarkCsv", "BenchmarkResult", "BenchmarkRunner", "ChaCha20Benchmark", "JceBenchmarkAlgorithm"]:
        copy_file(f"{J_BASE}/model/lab/{f}.java", f"app/{J_BASE}/model/lab/{f}.java")
    for f in ["BenchmarkController", "CaesarLabController", "PasswordLabController", "StepperController"]:
        copy_file(f"{J_BASE}/controller/lab/{f}.java", f"app/{J_BASE}/controller/lab/{f}.java")
    git_push(*ILIAS, "feat/lab", "LAB-01...09: Σύγκριση αλγορίθμων, κρυπτανάλυση Caesar και οπτικοποίηση")

    for f in ["HistoryController", "SettingsController"]:
        copy_file(f"{J_BASE}/controller/general/{f}.java", f"app/{J_BASE}/controller/general/{f}.java")
    copy_file(f"{J_BASE}/view/TutorialDialog.java", f"app/{J_BASE}/view/TutorialDialog.java")
    copy_file(f"{J_BASE}/AppStartup.java", f"app/{J_BASE}/AppStartup.java")
    copy_file(f"{J_BASE}/view/Screen.java", f"app/{J_BASE}/view/Screen.java")
    git_push(*ILIAS, "feat/general", "APP-06...12: Ρυθμίσεις, Ιστορικό και Οδηγός πρώτης χρήσης")

    # 2. ΛΕΩΝΙΔΑΣ: FXML Εργαστηρίου & Γενικών
    for f in ["lab.fxml", "lab_bench.fxml", "lab_caesar.fxml", "lab_password.fxml", "lab_steps.fxml", "general.fxml", "settings.fxml", "history.fxml"]:
        copy_file(f"{R_BASE}/view/{f}", f"app/{R_BASE}/view/{f}")
    git_push(*LEONIDAS, "ui/lab-general", "UI-07/UI-08: Οθόνες Εργαστηρίου και Γενικών ρυθμίσεων")

    # 3. ΑΠΟΣΤΟΛΟΣ: Tests Εργαστηρίου + Screenshot Tools
    copy_file(f"{T_BASE}/model/analysis/AnalysisTest.java", f"app/{T_BASE}/model/analysis/AnalysisTest.java")
    copy_file(f"{T_BASE}/model/lab/BenchmarkTest.java", f"app/{T_BASE}/model/lab/BenchmarkTest.java")
    for f in ["DemoData", "ScreenshotTool", "UiRobot"]:
        copy_file(f"{J_BASE}/tools/{f}.java", f"app/{J_BASE}/tools/{f}.java")
    git_push(*APOSTOLOS, "test/final-run", "LAB-01...09: JUnit tests ανάλυσης, benchmark και αυτοματισμός screenshots")