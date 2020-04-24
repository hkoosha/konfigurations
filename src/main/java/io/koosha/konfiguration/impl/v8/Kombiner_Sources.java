package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.type.Kind;
import io.koosha.konfiguration.Konfiguration;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Sources {

    @NotNull
    private final Kombiner origin;

    private final Map<Handle, Konfiguration> sources
            = new HashMap<>();

    boolean has(@NotNull final String key,
                @Nullable final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        return this.sources
                .values()
                .stream()
                .filter(x -> x != this.origin)
                .anyMatch(k -> k.has(key, type));
    }

    Kombiner_Sources(@NotNull final Kombiner origin) {
        Objects.requireNonNull(origin, "origin");
        this.origin = origin;
    }

    @Contract(pure = true)
    @NotNull
    Stream<Konfiguration> stream() {
        return sources.values().stream();
    }

    @Contract(mutates = "this")
    void replace(@NotNull final Map<Handle, Konfiguration> s) {
        Objects.requireNonNull(s, "origin");
        this.sources.clear();
        this.sources.putAll(s);
    }

    @Contract(pure = true)
    @NotNull
    Map<Handle, Konfiguration> copy() {
        return new HashMap<>(this.sources);
    }

}
