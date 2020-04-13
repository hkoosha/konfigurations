package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.Typer;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Values {

    @NotNull
    private final Kombiner origin;

    @NotNull
    final Set<Typer<?>> issuedKeys = new HashSet<>();

    @NotNull
    final Map<Typer<?>, ? super Object> cache = new HashMap<>();

    Kombiner_Values(@NotNull final Kombiner origin) {
        Objects.requireNonNull(origin, "origin");
        this.origin = origin;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "RedundantCast"})
    <U> K<U> k(@NotNull final String key,
               @Nullable final Typer<U> type) {
        Objects.requireNonNull(key, "key");
        this.issue(key, type);
        return new Kombiner_K<>(this.origin, key, type == null ? (Typer) Typer._VOID : type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    <U> U v(@NotNull final String key,
            @Nullable final Typer<?> type,
            @SuppressWarnings("SameParameterValue") @Nullable final U def,
            @SuppressWarnings("SameParameterValue") final boolean mustExist) {
        Objects.requireNonNull(key, "key");
        final Typer<?> t = type == null ? Typer._VOID.withKey(key) : type.withKey(key);

        return this.origin.r(() -> {
            if (cache.containsKey(t))
                return ((U) cache.get(t));
            return this.origin.w(() -> ((U) v_(t, def, mustExist)));
        });
    }

    Object v_(@NotNull final Typer<?> key,
              final Object def,
              final boolean mustExist) {
        Objects.requireNonNull(key, "key");
        final String keyStr = key.key();
        Objects.requireNonNull(keyStr, "key passed through kombiner is null");
        final Optional<Konfiguration> first = this
                .origin
                .sources
                .vs()
                .filter(source -> source.has(keyStr, key))
                .findFirst();
        if (!first.isPresent() && mustExist)
            throw new KfgMissingKeyException(this.origin.name(), keyStr, key);
        this.issue(keyStr, key);
        if (!first.isPresent())
            return def;
        final Object value = first.get().custom(keyStr, key).v();
        this.cache.put(key, value);
        return value;
    }

    boolean has(@NotNull final Typer<?> t) {
        Objects.requireNonNull(t.key());
        return this.cache.containsKey(t);
    }

    private void issue(@NotNull final String key,
                       @Nullable final Typer<?> typer) {
        Objects.requireNonNull(key, "key");
        this.issuedKeys.add(typer == null ? Typer._VOID.withKey(key) : typer.withKey(key));
    }


    @NotNull
    Map<Typer<?>, Object> copy() {
        return new HashMap<>(this.cache);
    }

    void replace(@NotNull final Map<Typer<?>, Object> copy) {
        Objects.requireNonNull(copy, "copy");
        this.cache.clear();
        this.cache.putAll(copy);
    }

    void origForEach(Consumer<Typer<?>> action) {
        this.issuedKeys.forEach(action);
    }

}
