package ispd.policy.loaders;

import ispd.arquivo.xml.ConfiguracaoISPD;
import ispd.policy.Policy;
import ispd.policy.PolicyLoader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GenericPolicyLoader <T extends Policy<?>> implements PolicyLoader<T> {

    private static final URLClassLoader CLASS_LOADER = makeClassLoader();

    private static URLClassLoader makeClassLoader () {
        try {
            return URLClassLoader.newInstance(
                new URL[] { ConfiguracaoISPD.DIRETORIO_ISPD.toURI().toURL(), },
                GenericPolicyLoader.class.getClassLoader()
            );
        } catch (final MalformedURLException ex) {
            Logger.getLogger(GenericPolicyLoader.class.getName())
                .log(Level.SEVERE, "Could not create the loader!", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    protected abstract String getClassPath ();

    @Override
    public T loadPolicy (final String policyName) {
        final var clsName = this.getClassPath() + policyName;
        try {
            final var cls = GenericPolicyLoader.CLASS_LOADER.loadClass(clsName);
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
