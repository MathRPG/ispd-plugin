package ispd.policy.loaders;

import ispd.arquivo.xml.*;
import ispd.policy.*;
import java.net.*;
import java.util.*;
import java.util.function.*;
import org.jetbrains.annotations.*;

public abstract class GenericPolicyLoader <T extends Policy<?>> implements PolicyLoader<T> {

    private static final ClassLoader CLASS_LOADER = GenericPolicyLoader.class.getClassLoader();

    private static final URLClassLoader URL_CLASS_LOADER = makeClassLoader();

    private static URLClassLoader makeClassLoader () {
        return URLClassLoader.newInstance(new URL[] { getIspdDirectoryUrl() }, CLASS_LOADER);
    }

    private static @NotNull URL getIspdDirectoryUrl () {
        try {
            return ConfiguracaoISPD.DIRETORIO_ISPD.toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public T loadPolicy (final String policyName) {
        return Optional.of(policyName)
            .map(this.getSupplierMap()::get)
            .map(Supplier::get)
            .orElseThrow(() -> new UnknownPolicyException(policyName));
    }

    protected abstract String getClassPath ();

    protected abstract @NotNull Map<String, Supplier<T>> getSupplierMap ();
}
