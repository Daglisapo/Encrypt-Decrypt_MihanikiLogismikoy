package gr.ihu.omada12.crypto.model.signature;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.crypto.PasswordBox;
import gr.ihu.omada12.crypto.model.io.FileProcessor;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.nio.file.Path;
import java.util.Arrays;

// Υλοποιεί: SIG-01 (το ιδιωτικό κλειδί προστατεύεται με κωδικό), NFR-03

/**
 * Αρχείο ιδιωτικού κλειδιού (.o12key): "O12K" + PasswordBox( μήκος + PKCS#8 ιδιωτικό + X.509 δημόσιο ).
 * Κρατάμε και το δημόσιο μέσα, ώστε η υπογραφή να γράφει το αποτύπωμά του (SIG-06).
 * Το ιδιωτικό κλειδί δεν γράφεται ΠΟΤΕ στον δίσκο χωρίς κρυπτογράφηση.
 */
public class PrivateKeyStore {
    public static final String EXTENSION = ".o12key";
    private static final byte[] MAGIC = {'O', '1', '2', 'K'};

    private final PasswordBox box = new PasswordBox();
    private final FileProcessor files = new FileProcessor();

    public void save(KeyPair pair, Path file, char[] password) throws CryptoException {
        byte[] priv = pair.getPrivate().getEncoded();
        byte[] pub = pair.getPublic().getEncoded();
        byte[] plain = ByteBuffer.allocate(4 + priv.length + pub.length).putInt(priv.length).put(priv).put(pub).array();
        byte[] sealed = box.seal(plain, password);
        Arrays.fill(plain, (byte) 0);
        files.write(file, ByteBuffer.allocate(MAGIC.length + sealed.length).put(MAGIC).put(sealed).array());
    }

    public KeyPair load(Path file, char[] password) throws CryptoException {
        byte[] all = files.read(file);
        if (all.length < MAGIC.length || !Arrays.equals(Arrays.copyOf(all, MAGIC.length), MAGIC)) {
            throw new CryptoException("error.notPrivateKey");
        }
        byte[] plain;
        try {
            plain = box.open(Arrays.copyOfRange(all, MAGIC.length, all.length), password);
        } catch (CryptoException e) {
            throw new CryptoException("error.wrongKeyPassword");
        }
        return decode(plain);
    }

    private KeyPair decode(byte[] plain) throws CryptoException {
        try {
            ByteBuffer buf = ByteBuffer.wrap(plain);
            byte[] priv = new byte[buf.getInt()];
            buf.get(priv);
            byte[] pub = new byte[buf.remaining()];
            buf.get(pub);
            KeyFactory kf = SignatureAlgorithm.keyFactory();
            return new KeyPair(kf.generatePublic(new X509EncodedKeySpec(pub)),
                    kf.generatePrivate(new PKCS8EncodedKeySpec(priv)));
        } catch (GeneralSecurityException | RuntimeException e) {
            throw new CryptoException("error.notPrivateKey");
        } finally {
            Arrays.fill(plain, (byte) 0);
        }
    }
}
