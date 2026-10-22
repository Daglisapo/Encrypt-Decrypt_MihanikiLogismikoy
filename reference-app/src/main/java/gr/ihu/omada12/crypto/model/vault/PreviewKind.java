package gr.ihu.omada12.crypto.model.vault;

import java.util.Locale;

// Υλοποιεί: VLT-14

/** Τι προεπισκόπηση μπορεί να γίνει από τον τύπο του αρχείου. */
public enum PreviewKind {
    IMAGE, TEXT, NONE;

    public static PreviewKind of(String type) {
        String t = type == null ? "" : type.toLowerCase(Locale.ROOT);
        if (t.matches("png|jpg|jpeg|gif|bmp")) return IMAGE;
        if (t.matches("txt|md|csv|json|xml|log|java|properties|html")) return TEXT;
        return NONE;
    }
}
