package gr.ihu.omada12.crypto.view;

import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.List;

// Υλοποιεί: VLT-05, VLT-13 (λίστες επιλογής φίλτρου τύπου/ετικέτας)

/** Λίστα επιλογής φίλτρου με πρώτη επιλογή «(όλα)», που ενημερώνεται από τις τιμές που υπάρχουν. */
public class ChoiceFilter {
    private final ComboBox<String> box;
    private final String all;

    public ChoiceFilter(ComboBox<String> box, String allText, Runnable onChange) {
        this.box = box;
        this.all = allText;
        box.getItems().setAll(List.of(allText));
        box.setValue(allText);
        box.valueProperty().addListener((o, a, b) -> onChange.run());
    }

    /** Νέες τιμές· η τρέχουσα επιλογή κρατιέται αν υπάρχει ακόμη. */
    public void update(List<String> values) {
        List<String> items = new ArrayList<>();
        items.add(all);
        items.addAll(values);
        if (items.equals(box.getItems())) return;
        String selected = box.getValue();
        box.getItems().setAll(items);
        box.setValue(items.contains(selected) ? selected : all);
    }

    /** Η επιλογή, ή κενό για «(όλα)». */
    public String value() {
        String v = box.getValue();
        return v == null || v.equals(all) ? "" : v;
    }

    public void reset() { box.setValue(all); }
}
