package gr.ihu.omada12.crypto.view;

import gr.ihu.omada12.crypto.model.history.AppModule;
import gr.ihu.omada12.crypto.model.vault.VaultSession;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

// Υλοποιεί: VLT-08

/**
 * Κάθε κίνηση ποντικιού, πλήκτρο ή κύλιση «μηδενίζει» τον χρόνο αδράνειας. Κάθε δευτερόλεπτο
 * ελέγχουμε αν πέρασε το όριο των ρυθμίσεων· αν ναι, το θησαυροφυλάκιο κλειδώνει αυτόματα.
 */
public class AutoLockMonitor {
    private final AppContext ctx;
    private final Timeline timer;

    public AutoLockMonitor(Scene scene, AppContext ctx) {
        this.ctx = ctx;
        VaultSession session = ctx.getVaultSession();
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> session.touch());
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> session.touch());
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> session.touch());
        scene.addEventFilter(ScrollEvent.SCROLL, e -> session.touch());
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> check()));
        timer.setCycleCount(Animation.INDEFINITE);
    }

    public void start() { timer.play(); }

    private void check() {
        VaultSession session = ctx.getVaultSession();
        String name = session.isOpen() ? session.current().getName() : "";
        if (!session.lockIfIdle()) return;
        PreviewWindow.closeAll();
        ctx.getHistory().record(AppModule.VAULT, "action.vaultAutoLock", name, true);
        if (ctx.getNavigator().current() == Screen.VAULT) ctx.getNavigator().replace(Screen.VAULT_LIST);
        ctx.dialogs().info("vault.autoLocked", name);
    }
}
