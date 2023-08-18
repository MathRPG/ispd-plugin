package ispd;

import ispd.application.*;
import ispd.application.terminal.*;
import java.util.*;

public enum Main {
    ;

    private static final Locale EN_US_LOCALE = new Locale("en", "US");

    public static void main (final String[] args) {
        setDefaultLocale();

        final var app = (args.length == 0)
                        ? new GuiApplication()
                        : new TerminalApplication(args);

        app.run();
    }

    private static void setDefaultLocale () {
        Locale.setDefault(Main.EN_US_LOCALE);
    }
}
