package io.koosha.konfiguration.impl.v0;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.koosha.konfiguration.Deserializer;
import io.koosha.konfiguration.Factory;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationBuilder;
import lombok.NonNull;
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
    public KonfigurationBuilder builder(@NotNull @NonNull final String name) {
        return new Kombiner_Builder(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_, _ -> new")
    @NotNull
    public Konfiguration kombine(@NotNull @NonNull final String name,
                                 @NotNull @NonNull final Konfiguration k0) {
        return kombine(name, singleton(k0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration kombine(@NonNull @NotNull final String name,
                                 @NonNull @NotNull final Konfiguration k0,
                                 @NonNull @NotNull final Konfiguration... sources) {
        final List<Konfiguration> l = new ArrayList<>();
        l.add(k0);
        l.addAll(asList(sources));
        return new Kombiner(name, l, LOCK_WAIT_MILLIS__DEFAULT, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration kombine(@NotNull @NonNull final String name,
                                 @NonNull @NotNull final Collection<Konfiguration> sources) {
        return new Kombiner(name, sources, LOCK_WAIT_MILLIS__DEFAULT, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_ -> new")
    public Konfiguration kombine(@NotNull @NonNull final Konfiguration k0) {
        return kombine(DEFAULT_KONFIG_NAME, k0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_, _ -> new")
    public Konfiguration kombine(@NotNull @NonNull final Konfiguration k0,
                                 @NotNull @NonNull final Konfiguration... sources) {
        return kombine(DEFAULT_KONFIG_NAME, k0, sources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_ -> new")
    public Konfiguration kombine(@NotNull @NonNull final Collection<Konfiguration> sources) {
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
    public Konfiguration map(@NotNull @NonNull final String name,
                             @NotNull @NonNull final Supplier<Map<String, ?>> storage) {
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
    public Konfiguration map(@NotNull @NonNull final String name,
                             @NotNull @NonNull final Map<String, ?> storage) {
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
    public Konfiguration map_(@NotNull @NonNull final Map<String, ?> storage) {
        return map(DEFAULT_KONFIG_NAME, storage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    public Konfiguration map_(@NotNull @NonNull final Supplier<Map<String, ?>> storage) {
        return map(DEFAULT_KONFIG_NAME, storage);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    public Konfiguration mapWithNested(@NotNull @NonNull final String name,
                                       @NotNull @NonNull final Supplier<Map<String, ?>> storage) {
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
    public Konfiguration mapWithNested(@NotNull @NonNull final String name,
                                       @NotNull @NonNull final Map<String, ?> storage) {
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
    public Konfiguration mapWithNested_(@NotNull @NonNull final Map<String, ?> storage) {
        return mapWithNested(DEFAULT_KONFIG_NAME, storage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public Konfiguration mapWithNested_(@NotNull @NonNull final Supplier<Map<String, ?>> storage) {
        return mapWithNested(DEFAULT_KONFIG_NAME, storage);
    }


    // ============================================================ PREFERENCES

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_ -> new")
    public Konfiguration preferences_(@NotNull @NonNull final Preferences storage) {
        return preferences(DEFAULT_KONFIG_NAME, storage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration preferences(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final Preferences storage) {
        final Konfiguration k = new ExtPreferencesSource(name, storage, null);
        return kombine(name, k);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration preferences_(@NotNull @NonNull final Preferences storage,
                                      @NotNull @NonNull final Deserializer deser) {
        return preferences(DEFAULT_KONFIG_NAME, storage, deser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration preferences(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final Preferences storage,
                                     @NotNull @NonNull final Deserializer deser) {
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
    public Konfiguration jacksonJson(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final Supplier<String> json) {
        final ObjectMapper mapper = ExtJacksonJsonSource.defaultJacksonObjectMapper();
        return jacksonJson(name, json, () -> mapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_ -> new")
    public Konfiguration jacksonJson_(@NotNull @NonNull final String json) {
        return jacksonJson(DEFAULT_KONFIG_NAME, json);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonJson_(@NonNull @NotNull final String json,
                                      @NonNull @NotNull final Supplier<ObjectMapper> objectMapper) {
        return jacksonJson(DEFAULT_KONFIG_NAME, json, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract("_ -> new")
    public Konfiguration jacksonJson_(@NotNull @NonNull final Supplier<String> json) {
        return jacksonJson(DEFAULT_KONFIG_NAME, json);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonJson_(@NotNull @NonNull final Supplier<String> json,
                                      @NotNull @NonNull final Supplier<ObjectMapper> objectMapper) {
        return jacksonJson(DEFAULT_KONFIG_NAME, json, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonJson(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final String json) {
        return jacksonJson(name, () -> json);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration jacksonJson(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final String json,
                                     @NonNull @NotNull final Supplier<ObjectMapper> objectMapper) {
        return jacksonJson(name, () -> json, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration jacksonJson(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final Supplier<String> json,
                                     @NonNull @NotNull final Supplier<ObjectMapper> objectMapper) {
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
    public Konfiguration snakeYaml_(@NotNull @NonNull final String yaml) {
        return snakeYaml(DEFAULT_KONFIG_NAME, yaml);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml_(@NotNull @NonNull final String yaml,
                                    @NotNull @NonNull final Supplier<Yaml> objectMapper) {
        return snakeYaml(DEFAULT_KONFIG_NAME, yaml, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_ -> new")
    public Konfiguration snakeYaml_(@NotNull @NonNull final Supplier<String> yaml) {
        return snakeYaml(DEFAULT_KONFIG_NAME, yaml);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml_(@NotNull @NonNull final Supplier<String> yaml,
                                    @NotNull @NonNull final Supplier<Yaml> objectMapper) {
        return snakeYaml(DEFAULT_KONFIG_NAME, yaml, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml(@NotNull @NonNull final String name,
                                   @NotNull @NonNull final String yaml) {
        return snakeYaml(name, () -> yaml);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration snakeYaml(@NonNull @NotNull final String name,
                                   @NotNull @NonNull final String yaml,
                                   @NotNull @NonNull final Supplier<Yaml> objectMapper) {
        return snakeYaml(name, () -> yaml, objectMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml(@NotNull @NonNull final String name,
                                   @NotNull @NonNull final Supplier<String> yaml) {
        final ExtYamlSource k = new ExtYamlSource(name, yaml, ExtYamlSource.defaultYamlSupplier::get, false);
        return kombine(name, k);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration snakeYaml(@NotNull @NonNull final String name,
                                   @NotNull @NonNull final Supplier<String> yaml,
                                   @NonNull @NotNull final Supplier<Yaml> objectMapper) {
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
    public Konfiguration snakeYaml_Unsafe(@NotNull @NonNull final String name,
                                          @NotNull @NonNull final Supplier<String> yaml) {
        return snakeYaml_Unsafe(name, yaml, ExtYamlSource.defaultYamlSupplier::get);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration snakeYaml_Unsafe(@NotNull @NonNull final String name,
                                          @NotNull @NonNull final Supplier<String> yaml,
                                          @NonNull @NotNull final Supplier<Yaml> objectMapper) {
        final ExtYamlSource k = new ExtYamlSource(name, yaml, objectMapper, true);
        return kombine(name, k);
    }

}
