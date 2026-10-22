package gr.ihu.omada12.crypto.model.stego;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;

// Υλοποιεί: STG-03, STG-06

/** Τι είναι κρυμμένο στην εικόνα: κείμενο ή αρχείο, κρυπτογραφημένο ή όχι. */
public enum PayloadKind {
    TEXT(0, false, false),
    TEXT_ENCRYPTED(1, true, false),
    FILE(2, false, true),
    FILE_ENCRYPTED(3, true, true);

    private final int code;
    private final boolean encrypted;
    private final boolean file;

    PayloadKind(int code, boolean encrypted, boolean file) {
        this.code = code;
        this.encrypted = encrypted;
        this.file = file;
    }

    public int getCode() { return code; }

    public boolean isEncrypted() { return encrypted; }

    public boolean isFile() { return file; }

    public static PayloadKind of(boolean file, boolean encrypted) {
        for (PayloadKind k : values()) {
            if (k.file == file && k.encrypted == encrypted) return k;
        }
        return TEXT;
    }

    public static PayloadKind fromCode(int code) throws CryptoException {
        for (PayloadKind k : values()) {
            if (k.code == code) return k;
        }
        throw new CryptoException("error.noHiddenMessage");
    }
}
