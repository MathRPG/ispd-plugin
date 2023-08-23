package ispd.application;

import ispd.gui.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;

public class GuiApplication implements Application {

    private static final String LOOK_AND_FEEL = "javax.swing.plaf.nimbus.NimbusLookAndFeel";

    private static final Logger LOGGER = Logger.getLogger(GuiApplication.class.getName());

    private static void openGui () {
        final var splash     = new SplashWindow();
        final var mainWindow = initializeApplication();
        splash.dispose();
        mainWindow.setVisible(true);
    }

    private static MainWindow initializeApplication () {
        final var exceptionLogger = new LogExceptions(null);
        Thread.setDefaultUncaughtExceptionHandler(exceptionLogger);

        setGuiLookAndFeel();

        final var mainWindow = new MainWindow();

        exceptionLogger.setParentComponent(mainWindow);

        return mainWindow;
    }

    private static void setGuiLookAndFeel () {
        try {
            UIManager.setLookAndFeel(LOOK_AND_FEEL);
        } catch (final ClassNotFoundException |
                       IllegalAccessException |
                       InstantiationException |
                       UnsupportedLookAndFeelException e) {
            LOGGER.severe(e::getLocalizedMessage);
        }
    }

    @Override
    public void run () {
        TextSupplier.setInstance(ResourceBundle.getBundle("text.gui"));

        openGui();
    }
}
