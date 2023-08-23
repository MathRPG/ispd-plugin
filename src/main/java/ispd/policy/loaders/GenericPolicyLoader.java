package ispd.policy.loaders;

import ispd.arquivo.xml.*;
import ispd.policy.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.logging.*;
import org.jetbrains.annotations.*;

public abstract class GenericPolicyLoader <T extends Policy<?>> implements PolicyLoader<T> {

    private static final URL[] CLASS_LOADER_URL = { getIspdDirectoryUrl() };

    private static final URLClassLoader CLASS_LOADER = makeClassLoader();

    private static @NotNull URL getIspdDirectoryUrl () {
        try {
            return ConfiguracaoISPD.DIRETORIO_ISPD.toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static URLClassLoader makeClassLoader () {
        return URLClassLoader.newInstance(
            CLASS_LOADER_URL,
            GenericPolicyLoader.class.getClassLoader()
        );
    }

    protected abstract String getClassPath ();

    @Override
    public T loadPolicy (final String policyName) {
        final var clsName = this.getClassPath() + policyName;
        try {
            final var cls = CLASS_LOADER.loadClass(clsName);
            return (T) cls.getConstructor().newInstance();
        } catch (final ClassNotFoundException | InvocationTargetException |
                       InstantiationException | IllegalAccessException |
                       NoSuchMethodException | ClassCastException e) {
            Logger.getLogger(GenericPolicyLoader.class.getName())
                .log(Level.SEVERE, "Could not load policy '%s'!\n".formatted(policyName), e);

            throw new RuntimeException(e);
        }
    }
}
