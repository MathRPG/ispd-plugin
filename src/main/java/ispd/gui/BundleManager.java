package ispd.gui;

import java.util.*;

public enum BundleManager {
    ;

    static ResourceBundle getBundle () {
        return ResourceBundle.getBundle("ispd.idioma.Idioma", Locale.getDefault());
    }
}
