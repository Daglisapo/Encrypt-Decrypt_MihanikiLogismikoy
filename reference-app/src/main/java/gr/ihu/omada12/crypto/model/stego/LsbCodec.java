package gr.ihu.omada12.crypto.model.stego;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

import java.awt.image.BufferedImage;

// Υλοποιεί: STG-01, STG-02, STG-04

/**
 * Στεγανογραφία LSB: κάθε bit του μηνύματος μπαίνει στο λιγότερο σημαντικό bit
 * ενός καναλιού χρώματος (R, G, B). Κάθε κανάλι αλλάζει το πολύ κατά 1 στα 255,
 * άρα η εικόνα φαίνεται ίδια. Χωρητικότητα: 3 bits ανά pixel.
 * Σειρά: pixels γραμμή-γραμμή, κανάλια R→G→B, bits κάθε byte από το πιο σημαντικό.
 */
public class LsbCodec {
    private static final int CHANNELS = 3;

    public static long capacityBytes(int width, int height) {
        return (long) width * height * CHANNELS / 8;
    }

    /** Επιστρέφει ΝΕΑ εικόνα (ARGB) με κρυμμένα τα δεδομένα· η αρχική δεν αλλάζει. */
    public BufferedImage embed(BufferedImage cover, byte[] data) throws CryptoException {
        if (data.length > capacityBytes(cover.getWidth(), cover.getHeight())) {
            throw new CryptoException("error.stegoTooLarge", data.length, capacityBytes(cover.getWidth(), cover.getHeight()));
        }
        BufferedImage out = copyArgb(cover);
        for (int i = 0; i < data.length; i++) {
            for (int b = 0; b < 8; b++) {
                int bit = (data[i] >> (7 - b)) & 1;
                writeBit(out, (long) i * 8 + b, bit);
            }
        }
        return out;
    }

    /** Διαβάζει length bytes ξεκινώντας από το byte offset. */
    public byte[] extract(BufferedImage image, int offset, int length) throws CryptoException {
        if ((long) offset + length > capacityBytes(image.getWidth(), image.getHeight())) {
            throw new CryptoException("error.noHiddenMessage");
        }
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            int value = 0;
            for (int b = 0; b < 8; b++) {
                value = (value << 1) | readBit(image, ((long) offset + i) * 8 + b);
            }
            data[i] = (byte) value;
        }
        return data;
    }

    private void writeBit(BufferedImage img, long bitIndex, int bit) {
        int pixel = (int) (bitIndex / CHANNELS);
        int x = pixel % img.getWidth();
        int y = pixel / img.getWidth();
        int shift = 16 - 8 * (int) (bitIndex % CHANNELS); // R=16, G=8, B=0
        int argb = img.getRGB(x, y);
        argb = (argb & ~(1 << shift)) | (bit << shift);
        img.setRGB(x, y, argb);
    }

    private int readBit(BufferedImage img, long bitIndex) {
        int pixel = (int) (bitIndex / CHANNELS);
        int shift = 16 - 8 * (int) (bitIndex % CHANNELS);
        return (img.getRGB(pixel % img.getWidth(), pixel / img.getWidth()) >> shift) & 1;
    }

    private BufferedImage copyArgb(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage(src, 0, 0, null);
        return copy;
    }
}
