package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.type.Kind;
import io.koosha.konfiguration.Konfiguration;
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
    final Set<Kind<?>> issuedKeys = new HashSet<>();

    @NotNull
    final Map<Kind<?>, ? super Object> cache = new HashMap<>();

    Kombiner_Values(@NotNull final Kombiner origin) {
        Objects.requireNonNull(origin, "origin");
        this.origin = origin;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "RedundantCast"})
    <U> K<U> k(@NotNull final String key,
               @Nullable final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        this.issue(key, type);
        return new Kombiner_K<>(this.origin, key, type == null ? (Kind) Kind._VOID : type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    <U> U v(@NotNull final String key,
            @Nullable final Kind<?> type,
            @SuppressWarnings("SameParameterValue") @Nullable final U def,
            @SuppressWarnings("SameParameterValue") final boolean mustExist) {
        Objects.requireNonNull(key, "key");
        final Kind<?> t = type == null ? Kind._VOID.withKey(key) : type.withKey(key);

        return this.origin.r(() -> {
            if (cache.containsKey(t))
                return ((U) cache.get(t));
            return this.origin.w(() -> (U) v_(t, def, mustExist));
        });
    }

    Object v_(@NotNull final Kind<?> key,
              final Object def,
              final boolean mustExist) {
        Objects.requireNonNull(key, "key");
        final String keyStr = key.key();
        Objects.requireNonNull(keyStr, "key passed through kombiner is null");
        final Optional<Konfiguration> find = this
                .origin
                .sources
                .stream()
                .filter(source -> source.has(keyStr, key))
                .findFirst();
        if (!find.isPresent() && mustExist)
            throw new KfgMissingKeyException(this.origin.name(), keyStr, key);
        this.issue(keyStr, key);
        if (!find.isPresent())
            return def;
        final Object value = find.get().custom(keyStr, key).v();
        this.cache.put(key, value);
        return value;
    }

    boolean has(@NotNull final Kind<?> t) {
        Objects.requireNonNull(t.key());
        return this.cache.containsKey(t);
    }

    private void issue(@NotNull final String key,
                       @Nullable final Kind<?> kind) {
        Objects.requireNonNull(key, "key");
        this.issuedKeys.add(kind == null ? Kind._VOID.withKey(key) : kind.withKey(key));
    }


    @NotNull
    Map<Kind<?>, Object> copy() {
        return new HashMap<>(this.cache);
    }

    void replace(@NotNull final Map<Kind<?>, Object> copy) {
        Objects.requireNonNull(copy, "copy");
        this.cache.clear();
        this.cache.putAll(copy);
    }

    void origForEach(Consumer<Kind<?>> action) {
        this.issuedKeys.forEach(action);
    }

}
