package io.koosha.konfiguration.impl;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@NotThreadSafe
@ApiStatus.Internal
final class KombinerSources {

    private final Map<Handle, Source> sources = new LinkedHashMap<>();


    boolean has(@NotNull final String key,
                @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        return this.sources
            .values()
            .stream()
            .anyMatch(k -> k.has(key, type));
    }

    @Contract(pure = true)
    @NotNull
    Collection<Source> sources() {
        return sources.values();
    }

    @Contract(mutates = "this")
    void replaceSources(@NotNull final LinkedHashMap<Handle, Source> s) {
        Objects.requireNonNull(s, "origin");
        this.sources.clear();
        this.sources.putAll(s);
    }

    @Contract(pure = true)
    @NotNull
    LinkedHashMap<Handle, Source> sourcesCopy() {
        return new LinkedHashMap<>(this.sources);
    }

}
