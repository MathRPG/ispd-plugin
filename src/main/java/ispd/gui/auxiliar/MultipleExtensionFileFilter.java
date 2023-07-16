package ispd.gui.auxiliar;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * FileFilter which allows multiple extensions for the same file type.
 */
public class MultipleExtensionFileFilter extends FileFilter {

    private final boolean allowDirs;

    private String description;

    private String[] extensions;

    public MultipleExtensionFileFilter (
        final String description,
        final String[] extensions,
        final boolean allowDirs
    ) {
        this.description = description;
        this.extensions  = extensions;
        this.allowDirs   = allowDirs;
    }

    public MultipleExtensionFileFilter (
        final String description,
        final String extension,
        final boolean allowDirs
    ) {
        this(description, new String[] { extension }, allowDirs);
    }

    @Override
    public boolean accept (final File file) {
        if (file.isDirectory() && this.allowDirs) {
            return true;
        }

        for (final String ext : this.extensions) {
            if (file.getName().toLowerCase().endsWith(ext)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getDescription () {
        return this.description;
    }

    public void setDescricao (final String description) {
        this.description = description;
    }

    public void setExtensao (final String[] extensions) {
        this.extensions = extensions;
    }

    public void setExtensao (final String extension) {
        this.extensions = new String[] { extension };
    }
}
