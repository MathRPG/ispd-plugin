package ispd.application;

import ispd.gui.*;
import ispd.gui.text.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.plaf.nimbus.*;

public class GuiApplication implements Application {

    private static final Logger LOGGER = Logger.getLogger(GuiApplication.class.getName());

    private static final String BUNDLE_LOCATION = "text.gui";

    private static void openGui () {
        final var splash     = new SplashWindow();
        final var mainWindow = initializeApplication();
        splash.dispose();
        mainWindow.setVisible(true);
    }

    private static MainWindow initializeApplication () {
        final var exceptionLogger = new LogExceptions();
        Thread.setDefaultUncaughtExceptionHandler(exceptionLogger);

        setGuiLookAndFeel();

        final var mainWindow = new MainWindow();

        exceptionLogger.setParentComponent(mainWindow);

        return mainWindow;
    }

    private static void setGuiLookAndFeel () {
        try {
            UIManager.setLookAndFeel(NimbusLookAndFeel.class.getCanonicalName());
        } catch (final ClassNotFoundException |
                       IllegalAccessException |
                       InstantiationException |
                       UnsupportedLookAndFeelException e) {
            LOGGER.severe(e::getLocalizedMessage);
        }
    }

    @Override
    public void run () {
        TextSupplier.configure(ResourceBundle.getBundle(BUNDLE_LOCATION));

        openGui();
    }
}
