package gr.ihu.omada12.crypto.view;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

// Υλοποιεί: APP-01, APP-02, APP-11

/**
 * Πλοήγηση ανάμεσα στις οθόνες. Κάθε οθόνη μπαίνει στο κέντρο ενός BorderPane και πάνω
 * υπάρχει πάντα η ίδια μπάρα: «Πίσω», «Αρχική», τίτλος, «?» (βοήθεια για τη συγκεκριμένη οθόνη).
 */
public class Navigator {
    private final AppContext context;
    private final BorderPane root = new BorderPane();
    private final Deque<Screen> backStack = new ArrayDeque<>();
    private final Label title = new Label();
    private final Button back = new Button();
    private final Button home = new Button();
    private final Button help = new Button("?");
    private Screen current;

    public Navigator(AppContext context) {
        this.context = context;
        root.getStyleClass().add("app-root");
        back.setOnAction(e -> back());
        home.setOnAction(e -> home());
        help.setOnAction(e -> showHelp());
        back.setId("nav-back");
        home.setId("nav-home");
        help.setId("nav-help");
        help.getStyleClass().add("help-button");
        title.getStyleClass().add("screen-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(8, back, home, title, spacer, help);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 12, 8, 12));
        bar.getStyleClass().add("top-bar");
        root.setTop(bar);
    }

    public Parent getRoot() { return root; }

    public Screen current() { return current; }

    public void show(Screen screen) {
        if (current != null && current != screen) backStack.push(current);
        load(screen);
    }

    /** Αλλαγή οθόνης χωρίς να μπει η τρέχουσα στο «Πίσω» (π.χ. αυτόματο κλείδωμα). */
    public void replace(Screen screen) { load(screen); }

    public void back() {
        if (!backStack.isEmpty()) load(backStack.pop());
    }

    public void home() {
        backStack.clear();
        load(Screen.HOME);
    }

    /** Μετά από αλλαγή γλώσσας: ξαναφορτώνει την τρέχουσα οθόνη με τα νέα κείμενα. */
    public void reload() { load(current == null ? Screen.HOME : current); }

    private void load(Screen screen) {
        I18n i18n = context.getI18n();
        FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/gr/ihu/omada12/crypto/view/" + screen.getFxml()),
                i18n.getBundle());
        loader.setControllerFactory(new ControllerFactory(context));
        try {
            root.setCenter(loader.<Parent>load());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load " + screen.getFxml(), e);
        }
        current = screen;
        title.setText(i18n.text(screen.getTitleKey()));
        back.setText("← " + i18n.text("nav.back"));
        home.setText(i18n.text("nav.home"));
        help.setTooltip(new Tooltip(i18n.text("nav.help")));
        back.setDisable(backStack.isEmpty());
        home.setDisable(screen == Screen.HOME);
    }

    private void showHelp() {
        I18n i18n = context.getI18n();
        context.dialogs().help(i18n.text(current.getTitleKey()), i18n.text(current.getHelpKey()));
    }
}
