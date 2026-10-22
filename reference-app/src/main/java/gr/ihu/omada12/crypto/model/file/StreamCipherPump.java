package gr.ihu.omada12.crypto.model.file;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import gr.ihu.omada12.crypto.model.io.ProgressListener;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

// Υλοποιεί: ENC-10 (πρόοδος και ακύρωση), NFR-06

/** Περνά ένα stream μέσα από Cipher σε κομμάτια των 64 KB, αναφέροντας πρόοδο. */
public class StreamCipherPump {
    static final int CHUNK = 64 * 1024;

    public void pump(InputStream in, OutputStream out, Cipher cipher, long total, ProgressListener listener)
            throws IOException, CryptoException {
        byte[] buffer = new byte[CHUNK];
        long done = 0;
        int n;
        while ((n = in.read(buffer)) != -1) {
            if (listener.isCancelled()) throw new CryptoException("error.cancelled");
            byte[] part = cipher.update(buffer, 0, n);
            if (part != null) out.write(part);
            done += n;
            listener.onProgress(done, total);
        }
        out.write(finish(cipher));
    }

    private byte[] finish(Cipher cipher) throws CryptoException {
        try {
            return cipher.doFinal();
        } catch (AEADBadTagException e) {
            throw new CryptoException("error.wrongKeyOrCorrupted", e);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("error.decryptFailed", e);
        }
    }
}
