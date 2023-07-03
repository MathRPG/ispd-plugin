package ispd.utils;

import ispd.utils.constants.FileExtensions;
import java.io.File;
import org.jetbrains.annotations.NotNull;

public class FileUtils {

    public static String fileExtensionOf (final File file) {
        return fileExtensionOf(file.getName());
    }

    private static @NotNull String fileExtensionOf (final String fileName) {
        final int i = fileName.lastIndexOf(FileExtensions.SEPARATOR);

        /* Must avoid 'extension only' files such as .gitignore
         * Also invalid are files that end with a '.'
         */
        if ((i <= 0) || (i >= (fileName.length() - 1))) {
            return "";
        }

        return fileName.substring(i + 1).toLowerCase();
    }
}
