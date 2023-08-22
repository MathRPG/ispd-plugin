package ispd.gui;

import java.util.*;

public enum BundleManager {
    ;

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("text.gui");

    public static String getText (final String key) {
        return BUNDLE.getString(key);
    }

    public static String tryGetText (final String text) {
        if (!BUNDLE.containsKey(text)) {
            return text;
        }

        return getText(text);
    }
}
