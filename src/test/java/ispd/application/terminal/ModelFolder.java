package ispd.application.terminal;

import java.nio.file.*;

public enum ModelFolder {
    NO_TYPE("notype"),
    GRID("grid"),
    ;

    private final Path path;

    ModelFolder (final String folderName) {
        this.path = TerminalApplicationCharacterizationTest.MODEL_FOLDER_PATH.resolve(folderName);
    }

    public String pathTo (final String modelName) {
        return this.path.resolve(modelName).toString();
    }
}
