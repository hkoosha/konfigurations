package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.Q;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Sources {

    @NotNull
    @NonNull
    private final Kombiner origin;

    private final Map<Handle, Konfiguration> sources
            = new HashMap<>();

    boolean has(@NotNull @NonNull final String key,
                @Nullable final Q<?> type) {
        return this.sources
                .values()
                .stream()
                .filter(x -> x != this.origin)
                .anyMatch(k -> k.has(key, type));
    }

    @Contract(pure = true)
    @NotNull
    Stream<Konfiguration> vs() {
        return sources.values().stream();
    }

    @Contract(pure = true)
    @NotNull
    Stream<Map.Entry<Handle, Konfiguration>> es() {
        return sources.entrySet().stream();
    }

    @Contract(mutates = "this")
    @NotNull
    Kombiner_Sources replace(@NotNull @NonNull final Map<Handle, Konfiguration> s) {
        this.sources.clear();
        this.sources.putAll(s);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    Map<Handle, Konfiguration> copy() {
        return new HashMap<>(this.sources);
    }

}
