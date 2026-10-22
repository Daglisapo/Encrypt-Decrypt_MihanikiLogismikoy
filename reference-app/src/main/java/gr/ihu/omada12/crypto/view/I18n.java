package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

// Υλοποιεί: APP-04, APP-06, NFR-09

/**
 * Όλα τα κείμενα της διεπαφής έρχονται από τα messages_el.properties / messages_en.properties.
 * Το μοντέλο στέλνει μόνο κλειδιά (π.χ. "error.wrongKeyOrCorrupted") και εδώ γίνονται κείμενο.
 */
public class I18n {
    public static final String BUNDLE = "gr.ihu.omada12.crypto.i18n.messages";

    private final ResourceBundle bundle;
    private final Locale locale;

    public I18n(String language) {
        this.locale = Locale.forLanguageTag(language);
        // Χωρίς fallback στη γλώσσα του λειτουργικού: «el» → messages_el, «en» → messages_en
        this.bundle = ResourceBundle.getBundle(BUNDLE, locale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
    }

    public ResourceBundle getBundle() { return bundle; }

    public Locale getLocale() { return locale; }

    /** Κείμενο για το κλειδί, με τιμές στις θέσεις {0}, {1}… Αν λείπει το κλειδί, εμφανίζεται το ίδιο. */
    public String text(String key, Object... args) {
        try {
            String pattern = bundle.getString(key);
            return args.length == 0 ? pattern : new MessageFormat(pattern, locale).format(args);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public String error(CryptoException e) {
        return text(e.getMessageKey(), e.getArgs());
    }
}
