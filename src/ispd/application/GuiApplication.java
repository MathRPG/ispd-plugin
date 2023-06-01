package ispd.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ispd.gui.LogExceptions;
import ispd.gui.MainWindow;
import ispd.gui.SplashWindow;

public class GuiApplication implements Application {

    private static final String GUI_LOOK_AND_FEEL_CLASS_NAME = "javax.swing.plaf.nimbus.NimbusLookAndFeel";

    @Override
    public void run () {
        openGui();
    }

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
            UIManager.setLookAndFeel(GuiApplication.GUI_LOOK_AND_FEEL_CLASS_NAME);
        } catch (final ClassNotFoundException | IllegalAccessException | InstantiationException |
                       UnsupportedLookAndFeelException e) {
            logWithMainLogger(e);
        }
    }

    private static MainWindow buildMainWindow () {
        final var gui = new MainWindow();
        gui.setLocationRelativeTo(null);
        return gui;
    }

    private static void logWithMainLogger (final Throwable ex) {
        Logger.getLogger(GuiApplication.class.getName()).log(Level.SEVERE, null, ex);
    }
}