package ispd.application;

import ispd.gui.LogExceptions;
import ispd.gui.MainWindow;
import ispd.gui.SplashWindow;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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

        final var mainWindow = buildMainWindow();

        exceptionLogger.setParentComponent(mainWindow);

        return mainWindow;
    }

    private static void setGuiLookAndFeel () {
        try {
            UIManager.setLookAndFeel(GuiApplication.LOOK_AND_FEEL);
        } catch (final ClassNotFoundException |
                       IllegalAccessException |
                       InstantiationException |
                       UnsupportedLookAndFeelException e) {
            logThrowableSeverely(e);
        }
    }

    private static MainWindow buildMainWindow () {
        final var gui = new MainWindow();
        gui.setLocationRelativeTo(null);
        return gui;
    }

    private static void logThrowableSeverely (final Throwable t) {
        GuiApplication.LOGGER.log(Level.SEVERE, null, t);
    }

    @Override
    public void run () {
        openGui();
    }
}
