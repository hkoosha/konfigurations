package io.koosha.konfiguration.impl.v0;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.koosha.konfiguration.Deserializer;
import io.koosha.konfiguration.Factory;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationBuilder;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableMap;

@ThreadSafe
@Immutable
@ApiStatus.AvailableSince(Factory.VERSION_8)
public final class FactoryV0 implements Factory {

    static final String DEFAULT_KONFIG_NAME = "default_konfig";

    private FactoryV0() {
    }

    private static final Factory INSTANCE = new FactoryV0();

    private static final String VERSION = "io.koosha.konfiguration:7.0.0";

    @Contract(pure = true)
    @NotNull
    public static Factory defaultInstance() {
        return FactoryV0.INSTANCE;
    }

    @Contract(pure = true)
    @NotNull
    public String defaultKonfigName() {
        return DEFAULT_KONFIG_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    @NotNull
    public String getVersion() {
        return VERSION;
    }

    // ================================================================ KOMBINER

    @Override
    @Contract("_ ->new")
    @NotNull
    public KonfigurationBuilder builder(@NotNull final String name) {
        Objects.requireNonNull(name, "name");
        return new Kombiner_Builder(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_, _ -> new")
    @NotNull
    public Konfiguration kombine(@NotNull final String name,
                                 @NotNull final Konfiguration source) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(source , "source");
        return kombine(name, singleton(source));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration kombine(@NotNull final String name,
                                 @NotNull final Konfiguration source,
                                 @NotNull final Konfiguration... sources) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(source , "source");
        Objects.requireNonNull(sources , "sources");
        final List<Konfiguration> l = new ArrayList<>();
        l.add(source);
        l.addAll(asList(sources));
        return new Kombiner(name, l, LOCK_WAIT_MILLIS__DEFAULT, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration kombine(@NotNull final String name,
                                 @NotNull final Collection<Konfiguration> sources) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(sources , "sources");
        return new Kombiner(name, sources, LOCK_WAIT_MILLIS__DEFAULT, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_ -> new")
    public Konfiguration kombine(@NotNull final Konfiguration source) {
        Objects.requireNonNull(source , "source");
        return kombine(DEFAULT_KONFIG_NAME, source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_, _ -> new")
    public Konfiguration kombine(@NotNull final Konfiguration source,
                                 @NotNull final Konfiguration... sources) {
        Objects.requireNonNull(source , "source");
        Objects.requireNonNull(sources , "sources");
        return kombine(DEFAULT_KONFIG_NAME, source, sources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_ -> new")
    public Konfiguration kombine(@NotNull final Collection<Konfiguration> sources) {
        Objects.requireNonNull(sources , "sources");
        return kombine(DEFAULT_KONFIG_NAME, sources);
    }

    // ==================================================================== MAP

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public Konfiguration map(@NotNull final String name,
                             @NotNull final Supplier<Map<String, ?>> storage) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(storage, "storage");
        final ExtMapSource k = new ExtMapSource(name, storage, false);
        return kombine(name, k);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public Konfiguration map(@NotNull final String name,
                             @NotNull final Map<String, ?> storage) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(storage, "storage");
        final Map<String, ?> copy = unmodifiableMap(new HashMap<>(storage));
        return map(name, () -> copy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public Konfiguration map_(@NotNull final Map<String, ?> storage) {
        Objects.requireNonNull(storage, "storage");
        return map(DEFAULT_KONFIG_NAME, storage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public Konfiguration map_(@NotNull final Supplier<Map<String, ?>> storage) {
        Objects.requireNonNull(storage, "storage");
        return map(DEFAULT_KONFIG_NAME, storage);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    public Konfiguration mapWithNested(@NotNull final String name,
                                       @NotNull final Supplier<Map<String, ?>> storage) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(storage, "storage");
        final ExtMapSource k = new ExtMapSource(name, storage, true);
        return kombine(name, k);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    public Konfiguration mapWithNested(@NotNull final String name,
                                       @NotNull final Map<String, ?> storage) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(storage, "storage");
        final Map<String, ?> copy = unmodifiableMap(new HashMap<>(storage));
        return mapWithNested(name, () -> copy);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public Konfiguration mapWithNested_(@NotNull final Map<String, ?> storage) {
        Objects.requireNonNull(storage, "storage");
        return mapWithNested(DEFAULT_KONFIG_NAME, storage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public Konfiguration mapWithNested_(@NotNull final Supplier<Map<String, ?>> storage) {
        Objects.requireNonNull(storage, "storage");
        return mapWithNested(DEFAULT_KONFIG_NAME, storage);
    }


    // ============================================================ PREFERENCES

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_ -> new")
    public Konfiguration preferences_(@NotNull final Preferences storage) {
        Objects.requireNonNull(storage, "storage");
        return preferences(DEFAULT_KONFIG_NAME, storage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration preferences(@NotNull final String name,
                                     @NotNull final Preferences storage) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(storage, "storage");
        final Konfiguration k = new ExtPreferencesSource(name, storage, null);
        return kombine(name, k);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration preferences_(@NotNull final Preferences storage,
                                      @NotNull final Deserializer deser) {
        Objects.requireNonNull(storage, "storage");
        return preferences(DEFAULT_KONFIG_NAME, storage, deser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration preferences(@NotNull final String name,
                                     @NotNull final Preferences storage,
                                     @NotNull final Deserializer deser) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(storage, "storage");
        final Konfiguration k = new ExtPreferencesSource(name, storage, deser);
        return kombine(name, k);
    }


    // ================================================================ JACKSON

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonJson(@NotNull final String name,
                                     @NotNull final Supplier<String> json) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        final ObjectMapper mapper = ExtJacksonJsonSource.defaultJacksonObjectMapper();
        return jacksonJson(name, json, () -> mapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_ -> new")
    public Konfiguration jacksonJson_(@NotNull final String json) {
        Objects.requireNonNull(json, "json");
        return jacksonJson(DEFAULT_KONFIG_NAME, json);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonJson_(@NotNull final String json,
                                      @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");
        return jacksonJson(DEFAULT_KONFIG_NAME, json, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_ -> new")
    public Konfiguration jacksonJson_(@NotNull final Supplier<String> json) {
        Objects.requireNonNull(json, "json");
        return jacksonJson(DEFAULT_KONFIG_NAME, json);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonJson_(@NotNull final Supplier<String> json,
                                      @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");
        return jacksonJson(DEFAULT_KONFIG_NAME, json, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonJson(@NotNull final String name,
                                     @NotNull final String json) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        return jacksonJson(name, () -> json);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration jacksonJson(@NotNull final String name,
                                     @NotNull final String json,
                                     @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");
        return jacksonJson(name, () -> json, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration jacksonJson(@NotNull final String name,
                                     @NotNull final Supplier<String> json,
                                     @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");
        final ExtJacksonJsonSource k = new ExtJacksonJsonSource(name, json, objectMapper);
        return kombine(name, k);
    }

    // ============================================================= SNAKE YAML

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_ -> new")
    public Konfiguration snakeYaml_(@NotNull final String yaml) {
        Objects.requireNonNull(yaml, "yaml");
        return snakeYaml(DEFAULT_KONFIG_NAME, yaml);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml_(@NotNull final String yaml,
                                    @NotNull final Supplier<Yaml> objectMapper) {
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(objectMapper, "objectMapper");
        return snakeYaml(DEFAULT_KONFIG_NAME, yaml, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_ -> new")
    public Konfiguration snakeYaml_(@NotNull final Supplier<String> yaml) {
        Objects.requireNonNull(yaml, "yaml");
        return snakeYaml(DEFAULT_KONFIG_NAME, yaml);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml_(@NotNull final Supplier<String> yaml,
                                    @NotNull final Supplier<Yaml> objectMapper) {
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(objectMapper, "objectMapper");
        return snakeYaml(DEFAULT_KONFIG_NAME, yaml, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml(@NotNull final String name,
                                   @NotNull final String yaml) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        return snakeYaml(name, () -> yaml);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration snakeYaml(@NotNull final String name,
                                   @NotNull final String yaml,
                                   @NotNull final Supplier<Yaml> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(objectMapper, "objectMapper");
        return snakeYaml(name, () -> yaml, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml(@NotNull final String name,
                                   @NotNull final Supplier<String> yaml) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        final ExtYamlSource k = new ExtYamlSource(name, yaml, ExtYamlSource.defaultYamlSupplier::get, false);
        return kombine(name, k);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration snakeYaml(@NotNull final String name,
                                   @NotNull final Supplier<String> yaml,
                                   @NotNull final Supplier<Yaml> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(objectMapper, "objectMapper");
        final ExtYamlSource k = new ExtYamlSource(name, yaml, objectMapper, false);
        return kombine(name, k);
    }

    // ============================================================= Experimental

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml_Unsafe(@NotNull final String name,
                                          @NotNull final Supplier<String> yaml) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        return snakeYaml_Unsafe(name, yaml, ExtYamlSource.defaultYamlSupplier::get);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration snakeYaml_Unsafe(@NotNull final String name,
                                          @NotNull final Supplier<String> yaml,
                                          @NotNull final Supplier<Yaml> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(objectMapper, "objectMapper");
        final ExtYamlSource k = new ExtYamlSource(name, yaml, objectMapper, true);
        return kombine(name, k);
    }

}
