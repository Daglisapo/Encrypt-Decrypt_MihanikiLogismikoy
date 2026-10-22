package gr.ihu.omada12.crypto.tools;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.signature.FileSigner;
import gr.ihu.omada12.crypto.model.signature.PrivateKeyStore;
import gr.ihu.omada12.crypto.model.signature.PublicKeyPem;
import gr.ihu.omada12.crypto.model.signature.SignatureAlgorithm;
import gr.ihu.omada12.crypto.model.signature.SignatureFileCodec;
import gr.ihu.omada12.crypto.model.stego.StegoService;
import gr.ihu.omada12.crypto.model.vault.Vault;
import gr.ihu.omada12.crypto.model.vault.VaultManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Random;

// Υλοποιεί: (εργαλείο ανάπτυξης: δοκιμαστικά δεδομένα για τα screenshots)

/** Φτιάχνει δοκιμαστικά αρχεία σε προσωρινό φάκελο, χρησιμοποιώντας μόνο το μοντέλο. */
public class DemoData {
    static final char[] VAULT_PASSWORD = "Demo-Vault-2026!".toCharArray();
    static final char[] KEY_PASSWORD = "Demo-Key-2026!".toCharArray();
    static final String HIDDEN_MESSAGE = "Συνάντηση την Πέμπτη στις 18:00, βιβλιοθήκη ΔΙΠΑΕ.";

    private final Path dir;

    public DemoData(Path dir) {
        this.dir = dir;
    }

    public Path dir() { return dir; }

    public Path contract() { return dir.resolve("symvasi.txt"); }

    public Path contractCopy() { return dir.resolve("symvasi-antigrafo.txt"); }

    public Path signature() { return SignatureFileCodec.signatureFileFor(contract()); }

    public Path publicKey() { return dir.resolve("alice" + PublicKeyPem.EXTENSION); }

    public Path cover() { return dir.resolve("photo.png"); }

    public Path stegoImage() { return dir.resolve("photo-stego.png"); }

    public Path benchFile() { return dir.resolve("bench-4MB.bin"); }

    public void create(VaultManager vaults) throws IOException, CryptoException {
        Files.createDirectories(dir);
        Vault vault = vaults.create("Demo", VAULT_PASSWORD, VAULT_PASSWORD);
        vault.add("notes.txt", "Λίστα για το ταξίδι".getBytes(StandardCharsets.UTF_8), "personal");
        vault.add("passport.pdf", new byte[2048], "documents");
        vault.add("budget.csv", "item,cost\nhotel,320\n".getBytes(StandardCharsets.UTF_8), "");
        vault.lock();

        Files.writeString(contract(), "Ο Α πληρώνει στον Β 100 ευρώ έως 30/11/2026.", StandardCharsets.UTF_8);
        Files.copy(contract(), contractCopy());
        KeyPair keys = SignatureAlgorithm.generateKeyPair();
        new PrivateKeyStore().save(keys, dir.resolve("alice" + PrivateKeyStore.EXTENSION), KEY_PASSWORD);
        new PublicKeyPem().export(keys.getPublic(), publicKey());
        new SignatureFileCodec().write(new FileSigner().sign(contract(), keys), signature());

        ImageIO.write(gradient(480, 320), "png", cover().toFile());
        new StegoService().hideText(cover(), HIDDEN_MESSAGE, null, stegoImage());

        byte[] data = new byte[4 * 1024 * 1024];
        new Random(7).nextBytes(data);
        Files.write(benchFile(), data);
    }

    /** Αλλάζει ένα byte του συμβολαίου, για να φανεί η «Μη έγκυρη» υπογραφή. */
    public void tamperContract() throws IOException {
        Files.writeString(contract(), "Ο Α πληρώνει στον Β 900 ευρώ έως 30/11/2026.", StandardCharsets.UTF_8);
    }

    private static BufferedImage gradient(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = 40 + x * 180 / w;
                int g = 90 + y * 120 / h;
                int b = 200 - (x + y) * 120 / (w + h);
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return img;
    }
}
