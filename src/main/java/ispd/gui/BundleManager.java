package ispd.gui;

import java.util.*;

public enum BundleManager {
    ;

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("ispd.idioma.Idioma");

    public static ResourceBundle getBundle () {
        return BUNDLE;
    }
}
