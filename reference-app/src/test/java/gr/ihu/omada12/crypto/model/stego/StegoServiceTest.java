package gr.ihu.omada12.crypto.model.stego;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** STG-01 … STG-04, STG-06, NFR-08 */
class StegoServiceTest {
    private static final int W = 100;
    private static final int H = 80;

    @TempDir
    Path dir;
    private Path cover;
    private final StegoService stego = new StegoService();

    @BeforeEach
    void setUp() throws Exception {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) img.setRGB(x, y, (x * 2) << 16 | (y * 3) << 8 | ((x + y) & 0xFF));
        }
        cover = dir.resolve("cover.png");
        ImageIO.write(img, "png", cover.toFile());
    }

    @Test // STG-01 + STG-02: το μήνυμα επιστρέφει ίδιο και η εικόνα αλλάζει το πολύ κατά 1 ανά κανάλι
    void hiddenTextIsRecoveredAndImageLooksTheSame() throws Exception {
        Path out = stego.hideText(cover, "Συνάντηση στις 8, πλατεία Αριστοτέλους", null, dir.resolve("out.png"));
        assertEquals("Συνάντηση στις 8, πλατεία Αριστοτέλους", stego.reveal(out, null).getText());

        BufferedImage a = ImageIO.read(cover.toFile());
        BufferedImage b = ImageIO.read(out.toFile());
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int p = a.getRGB(x, y);
                int q = b.getRGB(x, y);
                for (int shift = 0; shift <= 16; shift += 8) {
                    assertTrue(Math.abs(((p >> shift) & 0xFF) - ((q >> shift) & 0xFF)) <= 1);
                }
            }
        }
    }

    @Test // NFR-08: η έξοδος είναι πάντα PNG
    void outputIsAlwaysPng() throws Exception {
        Path out = stego.hideText(cover, "hi", null, dir.resolve("out.jpg"));
        assertEquals("out.png", out.getFileName().toString());
        assertEquals(ImageFormatPolicy.Support.JPEG_WARNING, ImageFormatPolicy.check(Path.of("a.JPG")));
        assertEquals(ImageFormatPolicy.Support.SUPPORTED, ImageFormatPolicy.check(Path.of("a.bmp")));
        assertEquals(ImageFormatPolicy.Support.UNSUPPORTED, ImageFormatPolicy.check(Path.of("a.gif")));
    }

    @Test // STG-03: κρυπτογραφημένο μήνυμα
    void encryptedMessageNeedsTheRightPassword() throws Exception {
        Path out = stego.hideText(cover, "μυστικό", "pw-123".toCharArray(), dir.resolve("enc.png"));
        CryptoException none = assertThrows(CryptoException.class, () -> stego.reveal(out, null));
        assertEquals("error.stegoPasswordRequired", none.getMessageKey());
        CryptoException wrong = assertThrows(CryptoException.class, () -> stego.reveal(out, "λάθος".toCharArray()));
        assertEquals("error.wrongKeyOrCorrupted", wrong.getMessageKey());
        assertEquals("μυστικό", stego.reveal(out, "pw-123".toCharArray()).getText());
    }

    @Test // STG-04: χωρητικότητα
    void capacityIsReportedAndEnforced() throws Exception {
        BufferedImage img = stego.readImage(cover);
        assertEquals(W * H * 3 / 8 - StegoFrame.HEADER_LENGTH, stego.freeBytes(img, false));
        assertEquals(stego.freeBytes(img, false) - 44, stego.freeBytes(img, true));
        String tooLong = "x".repeat((int) stego.freeBytes(img, false) + 1);
        CryptoException e = assertThrows(CryptoException.class,
                () -> stego.hideText(cover, tooLong, null, dir.resolve("big.png")));
        assertEquals("error.stegoTooLarge", e.getMessageKey());
    }

    @Test // STG-06: μικρό αρχείο μέσα σε εικόνα
    void smallFileCanBeHidden() throws Exception {
        Path secret = Files.write(dir.resolve("κλειδί.bin"), new byte[]{9, 8, 7, 6, 5});
        Path out = stego.hideFile(cover, secret, null, dir.resolve("file.png"));
        RevealedPayload payload = stego.reveal(out, null);
        assertTrue(payload.isFile());
        assertEquals("κλειδί.bin", payload.getFile().getName());
        assertArrayEquals(new byte[]{9, 8, 7, 6, 5}, payload.getFile().getData());
    }

    @Test
    void imageWithoutMessageIsReported() {
        CryptoException e = assertThrows(CryptoException.class, () -> stego.reveal(cover, null));
        assertEquals("error.noHiddenMessage", e.getMessageKey());
    }
}
