package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.Q;
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
    final Set<Q<?>> issuedKeys = new HashSet<>();

    @NotNull
    final Map<Q<?>, ? super Object> cache = new HashMap<>();

    Kombiner_Values(@NotNull final Kombiner origin) {
        Objects.requireNonNull(origin, "origin");
        this.origin = origin;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "RedundantCast"})
    <U> K<U> k(@NotNull final String key,
               @Nullable final Q<U> type) {
        Objects.requireNonNull(key, "key");
        this.issue(key, type);
        return new Kombiner_K<>(this.origin, key, type == null ? (Q) Q._VOID : type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    <U> U v(@NotNull final String key,
            @Nullable final Q<?> type,
            @SuppressWarnings("SameParameterValue") @Nullable final U def,
            @SuppressWarnings("SameParameterValue") final boolean mustExist) {
        Objects.requireNonNull(key, "key");
        final Q<?> t = Q.withKey0(type, key);

        return this.origin.r(() -> {
            if (cache.containsKey(t))
                return ((U) cache.get(t));
            return this.origin.w(() -> ((U) v_(t, def, mustExist)));
        });
    }

    Object v_(@NotNull final Q<?> key,
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

    boolean has(@NotNull final Q<?> t) {
        Objects.requireNonNull(t.key());
        return this.cache.containsKey(t);
    }

    private void issue(@NotNull final String key,
                       @Nullable final Q<?> q) {
        this.issuedKeys.add(Q.withKey0(q, key));
    }


    @NotNull
    Map<Q<?>, Object> copy() {
        return new HashMap<>(this.cache);
    }

    void replace(@NotNull final Map<Q<?>, Object> copy) {
        Objects.requireNonNull(copy, "copy");
        this.cache.clear();
        this.cache.putAll(copy);
    }

    void origForEach(Consumer<Q<?>> action) {
        this.issuedKeys.forEach(action);
    }

}
