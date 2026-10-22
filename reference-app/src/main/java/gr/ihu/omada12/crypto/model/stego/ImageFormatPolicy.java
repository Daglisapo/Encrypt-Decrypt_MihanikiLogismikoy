package gr.ihu.omada12.crypto.model.stego;

import java.nio.file.Path;
import java.util.Locale;

// Υλοποιεί: NFR-08

/**
 * Η στεγανογραφία δέχεται PNG ή BMP (χωρίς απώλειες). Για JPEG δείχνουμε προειδοποίηση:
 * η είσοδος γίνεται δεκτή, αλλά η ΕΞΟΔΟΣ είναι πάντα PNG, γιατί η συμπίεση JPEG σβήνει τα bits.
 */
public final class ImageFormatPolicy {
    /** Πόσο κατάλληλη είναι μια εικόνα ως «φορέας». */
    public enum Support { SUPPORTED, JPEG_WARNING, UNSUPPORTED }

    private ImageFormatPolicy() { }

    public static Support check(Path image) {
        String n = image.getFileName().toString().toLowerCase(Locale.ROOT);
        if (n.endsWith(".png") || n.endsWith(".bmp")) return Support.SUPPORTED;
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return Support.JPEG_WARNING;
        return Support.UNSUPPORTED;
    }

    /** photo.jpg → photo.png, secret → secret.png */
    public static Path asPng(Path target) {
        String n = target.getFileName().toString();
        int dot = n.lastIndexOf('.');
        String base = dot > 0 ? n.substring(0, dot) : n;
        return target.resolveSibling(base + ".png");
    }
}
