package gr.ihu.omada12.crypto.view;

import javafx.util.Callback;

import java.lang.reflect.Constructor;

// Υλοποιεί: (υποδομή MVC)

/**
 * Το FXMLLoader φτιάχνει τους controllers μέσω αυτής της κλάσης, ώστε να τους δίνει το AppContext
 * στον κατασκευαστή (απλό dependency injection χωρίς βιβλιοθήκη). Ισχύει και για τα fx:include.
 */
public class ControllerFactory implements Callback<Class<?>, Object> {
    private final AppContext context;

    public ControllerFactory(AppContext context) {
        this.context = context;
    }

    @Override
    public Object call(Class<?> type) {
        try {
            for (Constructor<?> c : type.getConstructors()) {
                if (c.getParameterCount() == 1 && c.getParameterTypes()[0] == AppContext.class) {
                    return c.newInstance(context);
                }
            }
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create controller " + type.getName(), e);
        }
    }
}
