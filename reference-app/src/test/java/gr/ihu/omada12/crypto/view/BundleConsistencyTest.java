package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.password.CrackTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NFR-09, APP-06: τα δύο bundles έχουν ακριβώς τα ίδια κλειδιά και ΚΑΘΕ κλειδί που
 * χρησιμοποιείται στον κώδικα ή στα FXML υπάρχει. Έτσι δεν εμφανίζεται ποτέ «σκέτο» κλειδί.
 */
class BundleConsistencyTest {
    private static final Path RES = Path.of("src/main/resources/gr/ihu/omada12/crypto");
    private static final Path JAVA = Path.of("src/main/java");
    private static final Pattern JAVA_KEY = Pattern.compile(
            "\"((?:error|enc|vault|sig|stego|lab|pw|action|module|help|tutorial|settings|history|home|screen|common|filter|nav|exit|general|app)\\.[A-Za-z0-9_.]*[A-Za-z0-9_])\"");
    private static final Pattern FXML_KEY = Pattern.compile("\"%([A-Za-z0-9_.]+)\"");

    private static Properties el;
    private static Properties en;

    @BeforeAll
    static void load() throws IOException {
        el = read(RES.resolve("i18n/messages_el.properties"));
        en = read(RES.resolve("i18n/messages_en.properties"));
    }

    private static Properties read(Path file) throws IOException {
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(r);
        }
        return p;
    }

    @Test
    void greekAndEnglishHaveTheSameKeys() {
        assertEquals(new TreeSet<>(el.stringPropertyNames()), new TreeSet<>(en.stringPropertyNames()));
    }

    @Test
    void everyKeyUsedInJavaAndFxmlExists() throws IOException {
        Set<String> used = new TreeSet<>();
        used.addAll(scan(JAVA, ".java", JAVA_KEY));
        used.addAll(scan(RES.resolve("view"), ".fxml", FXML_KEY));
        used.removeIf(k -> k.endsWith(".fxml") || k.endsWith(".log") || k.endsWith(".properties"));
        List<String> missing = used.stream().filter(k -> !el.containsKey(k)).collect(Collectors.toList());
        assertTrue(missing.isEmpty(), "Λείπουν κλειδιά: " + missing);
    }

    @Test // κλειδιά που φτιάχνονται δυναμικά στον κώδικα
    void dynamicKeysExist() {
        List<String> keys = new ArrayList<>();
        for (Screen s : Screen.values()) {
            keys.add(s.getTitleKey());
            keys.add(s.getHelpKey());
        }
        for (AppModule m : AppModule.values()) keys.add(m.getKey());
        for (int i = 0; i <= 4; i++) keys.add("pw.level." + i);
        for (CrackTime.Unit u : CrackTime.Unit.values()) keys.add("pw.crack." + u.name().toLowerCase(Locale.ROOT));
        for (int i = 1; i <= TutorialDialog.PAGES; i++) {
            keys.add("tutorial." + i + ".title");
            keys.add("tutorial." + i + ".body");
        }
        List<String> missing = keys.stream().filter(k -> !el.containsKey(k)).collect(Collectors.toList());
        assertTrue(missing.isEmpty(), "Λείπουν κλειδιά: " + missing);
    }

    @Test
    void messagesWithArgumentsAreValidMessageFormatPatterns() {
        for (String key : el.stringPropertyNames()) {
            for (Properties p : List.of(el, en)) {
                String value = p.getProperty(key);
                if (value.contains("{0}")) {
                    assertTrue(!value.contains("'"), key + ": απόστροφος σε μήνυμα με παράμετρο");
                    new java.text.MessageFormat(value).format(new Object[]{"a", "b", "c", "d"});
                }
            }
        }
    }

    private static Set<String> scan(Path root, String ext, Pattern pattern) throws IOException {
        Set<String> keys = new TreeSet<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path f : files.filter(p -> p.toString().endsWith(ext)).collect(Collectors.toList())) {
                Matcher m = pattern.matcher(Files.readString(f, StandardCharsets.UTF_8));
                while (m.find()) keys.add(m.group(1));
            }
        }
        return keys;
    }
}
