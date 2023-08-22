package ispd.gui;

import java.util.*;

public enum BundleManager {
    ;

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("text.gui");

    public static ResourceBundle getBundle () {
        return BUNDLE;
    }

    static String getText (final String key) {
        return BUNDLE.getString(key);
    }
}
