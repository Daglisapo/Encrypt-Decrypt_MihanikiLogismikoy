package gr.ihu.omada12.crypto.view;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

// Υλοποιεί: ENC-09 (πρόχειρο)

/** Μικρές βοηθητικές λειτουργίες της διεπαφής που χρειάζονται πολλοί controllers. */
public final class Views {
    private Views() { }

    public static void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }
}
