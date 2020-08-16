package io.koosha.konfiguration.impl.v8;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationFactory;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableMap;

@ThreadSafe
@Immutable
@ApiStatus.Internal
public final class FactoryV8 implements KonfigurationFactory {

    private static final String VERSION = "io.koosha.konfiguration:8.0.0";

    private static final boolean DEFAULT_FAIR_LOCK = false;
    private static final Long DEFAULT_LOCK_WAIT_TIME_MILLIS = null;
    private static final boolean DEFAULT_UPDATABLE = true;

    @Contract(pure = true)
    @NotNull
    public static KonfigurationFactory getInstanceV8() {
        return getInstanceV8(
            DEFAULT_LOCK_WAIT_TIME_MILLIS,
            DEFAULT_FAIR_LOCK,
            DEFAULT_UPDATABLE);
    }

    @Contract(pure = true)
    @NotNull
    public static KonfigurationFactory getInstanceV8(@Nullable final Long lockWaitTime,
                                                     final boolean fairLock) {
        return getInstanceV8(lockWaitTime, fairLock, DEFAULT_UPDATABLE);
    }

    @Contract(pure = true)
    @NotNull
    public static KonfigurationFactory getInstanceV8(@Nullable final Long lockWaitTime,
                                                     final boolean fairLock,
                                                     final boolean updatable) {
        return new FactoryV8(lockWaitTime, fairLock, updatable);
    }

    @Nullable
    private final Long lockWaitTime;
    private final boolean fairLock;
    private final boolean updatable;

    private FactoryV8(@Nullable final Long lockWaitTime,
                      final boolean fairLock,
                      final boolean updatable) {
        this.lockWaitTime = lockWaitTime;
        this.fairLock = fairLock;
        this.updatable = updatable;
    }


    @Override
    @Contract(pure = true)
    @NotNull
    public String getVersion() {
        return VERSION;
    }

    // ================================================================ KOMBINER

    @Override
    @Contract("_, _ -> new")
    @NotNull
    public Konfiguration kombine(@NotNull final String name,
                                 @NotNull final Konfiguration source) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(source, "source");
        return kombine(name, singleton(source));
    }

    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration kombine(@NotNull final String name,
                                 @NotNull final Konfiguration source,
                                 @NotNull final Konfiguration... rest) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(rest, "sources");
        final List<Konfiguration> l = new ArrayList<>();
        l.add(source);
        l.addAll(asList(rest));
        return new Kombiner(name,
            l,
            this.lockWaitTime,
            this.fairLock,
            this.updatable);
    }

    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration kombine(@NotNull final String name,
                                 @NotNull final Collection<Konfiguration> sources) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(sources, "sources");
        return new Kombiner(name,
            sources,
            this.lockWaitTime,
            this.fairLock,
            this.updatable);
    }

    // ==================================================================== MAP

    @Override
    @NotNull
    @Contract(pure = true,
              value = "_, _ -> new")
    public Konfiguration map(@NotNull final String name,
                             @NotNull final Supplier<Map<String, ?>> storage) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(storage, "storage");
        final Konfiguration k = new ExtMapSource(name, storage);
        return kombine(name, k);
    }

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

    // ============================================================ PREFERENCES

    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration preferences(@NotNull final String name,
                                     @NotNull final Preferences storage) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(storage, "storage");
        final Konfiguration k = new ExtPreferencesSource(name, storage);
        return kombine(name, k);
    }

    // ================================================================ JACKSON

    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonJson(@NotNull final String name,
                                     @NotNull final Supplier<String> json) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        ExtJacksonSourceJsonHelper.ensureLibraryJarIsOnPath();

        return jacksonJson(name, json, ExtJacksonSourceJsonHelper::mapper);
    }

    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonJson(@NotNull final String name,
                                     @NotNull final String json) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        ExtJacksonSourceJsonHelper.ensureLibraryJarIsOnPath();

        return jacksonJson(name, () -> json);
    }

    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration jacksonJson(@NotNull final String name,
                                     @NotNull final String json,
                                     @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");
        ExtJacksonSourceJsonHelper.ensureLibraryJarIsOnPath();

        return jacksonJson(name, () -> json, objectMapper);
    }

    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration jacksonJson(@NotNull final String name,
                                     @NotNull final Supplier<String> json,
                                     @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");
        ExtJacksonSourceJsonHelper.ensureLibraryJarIsOnPath();

        final Konfiguration k = new ExtJacksonSource(name, json, objectMapper);
        return kombine(name, k);
    }


    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonYaml(@NotNull final String name,
                                     @NotNull final Supplier<String> yaml) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        ExtJacksonSourceYamlHelper.ensureLibraryJarIsOnPath();

        return jacksonYaml(name, yaml, ExtJacksonSourceYamlHelper::mapper);
    }

    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration jacksonYaml(@NotNull final String name,
                                     @NotNull final String yaml) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        ExtJacksonSourceYamlHelper.ensureLibraryJarIsOnPath();

        return jacksonYaml(name, () -> yaml);
    }

    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration jacksonYaml(@NotNull final String name,
                                     @NotNull final String yaml,
                                     @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(objectMapper, "objectMapper");
        ExtJacksonSourceYamlHelper.ensureLibraryJarIsOnPath();

        return jacksonYaml(name, () -> yaml, objectMapper);
    }

    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration jacksonYaml(@NotNull final String name,
                                     @NotNull final Supplier<String> yaml,
                                     @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(objectMapper, "objectMapper");
        ExtJacksonSourceYamlHelper.ensureLibraryJarIsOnPath();

        final Konfiguration k = new ExtJacksonSource(name, yaml, objectMapper);
        return kombine(name, k);
    }

    // ============================================================= SNAKE YAML

    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml(@NotNull final String name,
                                   @NotNull final String yaml) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        return snakeYaml(name, () -> yaml);
    }

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

    @Override
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration snakeYaml(@NotNull final String name,
                                   @NotNull final Supplier<String> yaml) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        return snakeYaml(name, yaml, ExtYamlSource.defaultYamlSupplier::get);
    }

    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration snakeYaml(@NotNull final String name,
                                   @NotNull final Supplier<String> yaml,
                                   @NotNull final Supplier<Yaml> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(objectMapper, "objectMapper");

        final Konfiguration k = new ExtYamlSource(name, yaml, objectMapper);
        return kombine(name, k);
    }

}
