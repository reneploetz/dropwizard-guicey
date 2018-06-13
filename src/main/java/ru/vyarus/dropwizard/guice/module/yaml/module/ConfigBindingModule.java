package ru.vyarus.dropwizard.guice.module.yaml.module;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.util.Providers;
import io.dropwizard.Configuration;
import ru.vyarus.dropwizard.guice.module.yaml.YamlConfig;
import ru.vyarus.dropwizard.guice.module.yaml.YamlConfigItem;

/**
 * Binds configuration constants. Bindings are qualified with {@link Config}.
 * <p>
 * Note that not all configuration paths may be available because configuration is introspected using jersey
 * serialization api and some configuration classes may only consume properties (e.g. value consumed directly in
 * setter - impossible to read back).
 * <p>
 * All content types are bound by declaration type (as they declared in configuration class). All primitive types
 * are boxed. All collection types like List, Set, Map, Multimap are bound by collection type (List, Set etc).
 * Generics are always used in binding (so {@code @inject @Config("path") List<String> val} will work and
 * {@code @inject @Config("path") List val} will not).
 * <p>
 * Root configuration objects are bound with and without qualifier, except root interfaces which are bound
 * with qualifier only by default. Direct interfaces binding could be enabled with
 * {@link ru.vyarus.dropwizard.guice.GuiceyOptions#BindConfigurationInterfaces}, but this is deprecated behaviour
 * (remained only for compatibility reasons).
 * <p>
 * {@link YamlConfig} instance is also bound directly to be used for custom configuration analysis.
 *
 * @author Vyacheslav Rusakov
 * @since 04.05.2018
 * @see Config for more info on usage
 */
public class ConfigBindingModule extends AbstractModule {

    private final Configuration configuration;
    private final YamlConfig info;
    private final boolean bindInterfaces;

    public ConfigBindingModule(final Configuration configuration,
                               final YamlConfig info,
                               final boolean bindInterfaces) {
        this.configuration = configuration;
        this.info = info;
        this.bindInterfaces = bindInterfaces;
    }

    @Override
    protected void configure() {
        bind(YamlConfig.class).toInstance(info);

        bindRootTypes();
        bindUniqueContentTypes();
        bindConstants();
    }


    /**
     * Bind configuration hierarchy: all superclasses and direct interfaces for each level (except common interfaces).
     * Interfaces are bound only with qualifier, except when deprecated option enabled.
     */
    @SuppressWarnings("unchecked")
    private void bindRootTypes() {
        for (Class type : info.getRootTypes()) {
            // bind root configuration classes both with and without qualifier
            if (!type.isInterface() || bindInterfaces) {
                // bind interface as type only when it's allowed
                bind(type).toInstance(configuration);
            }
            bind(type).annotatedWith(Config.class).toInstance(configuration);
        }
    }

    /**
     * Bind unique sub configuration objects by type. Available for injection like
     * {@code @Inject @Config MySubConf config}. Value may be null because if null values would be avoided,
     * bindings will disappear.
     */
    @SuppressWarnings("unchecked")
    private void bindUniqueContentTypes() {
        for (YamlConfigItem item : info.getUniqueContentTypes()) {
            // bind only with annotation to avoid clashes with direct bindings
            toValue(
                    bind(Key.get(item.getDeclaredTypeWithGenerics(), Config.class)),
                    item.getValue());
        }
    }

    /**
     * Bind configuration paths. Available for injection like {@code @Inject @Code("path.sub") Integer conf}.
     * Value may be null because if null values would be avoided, bindings will disappear.
     */
    @SuppressWarnings({"unchecked", "PMD.AvoidInstantiatingObjectsInLoops"})
    private void bindConstants() {
        for (YamlConfigItem item : info.getPaths()) {
            toValue(
                    bind(Key.get(item.getDeclaredTypeWithGenerics(), new ConfigImpl(item.getPath()))),
                    item.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void toValue(final LinkedBindingBuilder binding, final Object value) {
        if (value != null) {
            binding.toInstance(value);
        } else {
            binding.toProvider(Providers.of(null));
        }
    }
}