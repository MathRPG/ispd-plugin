package ispd.application.terminal;

import ispd.utils.constants.*;
import java.nio.file.*;
import org.jetbrains.annotations.*;

public enum ModelFolder {
    NO_TYPE("notype"),
    GRID("grid"),
    ;

    private final Path path;

    ModelFolder (final String folderName) {
        this.path = BasePathHolder.BASE_PATH.resolve(folderName);
    }

    public @NotNull String pathToModel (final String modelName) {
        return this.pathToFile(modelName + FileExtensions.ICONIC_MODEL);
    }

    public @NotNull String pathToFile (final String fileName) {
        return this.path.resolve(fileName).toString();
    }

    private static class BasePathHolder {

        private static final Path BASE_PATH = Path.of("src", "test", "resources", "models");
    }
}
