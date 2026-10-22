package gr.ihu.omada12.crypto.model.stego;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.crypto.PasswordBox;
import gr.ihu.omada12.crypto.model.io.AtomicFileWriter;
import gr.ihu.omada12.crypto.model.io.FileProcessor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

// Υλοποιεί: STG-01, STG-02, STG-03, STG-04, STG-06, NFR-08

/**
 * Συντονίζει τη στεγανογραφία: διαβάζει την εικόνα, (προαιρετικά) κρυπτογραφεί το μήνυμα με κωδικό,
 * το βάζει στο πλαίσιο StegoFrame, το κρύβει με LSB και γράφει ΠΑΝΤΑ PNG.
 */
public class StegoService {
    private final LsbCodec codec = new LsbCodec();
    private final PasswordBox box = new PasswordBox();
    private final FileProcessor files = new FileProcessor();
    private final AtomicFileWriter writer = new AtomicFileWriter();

    public BufferedImage readImage(Path file) throws CryptoException {
        try {
            BufferedImage img = ImageIO.read(file.toFile());
            if (img == null) throw new CryptoException("error.notImage", file.getFileName());
            return img;
        } catch (IOException e) {
            throw new CryptoException("error.notImage", file.getFileName());
        }
    }

    /** STG-04: πόσα bytes μηνύματος χωράνε (UTF-8: λατινικός χαρακτήρας = 1 byte, ελληνικός = 2). */
    public long freeBytes(BufferedImage image, boolean encrypted) {
        long free = LsbCodec.capacityBytes(image.getWidth(), image.getHeight()) - StegoFrame.HEADER_LENGTH
                - (encrypted ? PasswordBox.overhead() : 0);
        return Math.max(0, free);
    }

    public static int bytesOf(String text) {
        return text == null ? 0 : text.getBytes(StandardCharsets.UTF_8).length;
    }

    public Path hideText(Path cover, String text, char[] password, Path target) throws CryptoException {
        if (text == null || text.isEmpty()) throw new CryptoException("error.emptyText");
        return hide(cover, false, text.getBytes(StandardCharsets.UTF_8), password, target);
    }

    public Path hideFile(Path cover, Path secret, char[] password, Path target) throws CryptoException {
        HiddenFile hidden = new HiddenFile(secret.getFileName().toString(), files.read(secret));
        return hide(cover, true, hidden.encode(), password, target);
    }

    public RevealedPayload reveal(Path image, char[] password) throws CryptoException {
        BufferedImage img = readImage(image);
        long capacity = LsbCodec.capacityBytes(img.getWidth(), img.getHeight());
        StegoFrame frame = StegoFrame.parseHeader(codec.extract(img, 0, StegoFrame.HEADER_LENGTH), capacity);
        byte[] body = codec.extract(img, StegoFrame.HEADER_LENGTH, frame.getBodyLength());
        if (frame.getKind().isEncrypted()) {
            if (!hasPassword(password)) throw new CryptoException("error.stegoPasswordRequired");
            body = box.open(body, password);
        }
        return new RevealedPayload(frame.getKind(), body);
    }

    private Path hide(Path cover, boolean isFile, byte[] body, char[] password, Path target) throws CryptoException {
        BufferedImage img = readImage(cover);
        boolean encrypt = hasPassword(password);
        byte[] payload = encrypt ? box.seal(body, password) : body;
        byte[] frame = StegoFrame.build(PayloadKind.of(isFile, encrypt), payload);
        BufferedImage stego = codec.embed(img, frame);
        Path png = ImageFormatPolicy.asPng(target);
        writer.write(png, out -> {
            if (!ImageIO.write(stego, "png", out)) throw new CryptoException("error.fileWrite", png.getFileName());
        });
        return png;
    }

    private static boolean hasPassword(char[] password) {
        return password != null && password.length > 0;
    }
}
