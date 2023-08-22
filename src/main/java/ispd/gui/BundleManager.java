package ispd.gui;

import java.util.*;

public enum BundleManager {
    ;

    public static ResourceBundle getBundle (final Locale locale) {
        return ResourceBundle.getBundle("ispd.idioma.Idioma", locale);
    }
}
