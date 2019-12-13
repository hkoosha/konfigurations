package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.Q;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@AllArgsConstructor
@Accessors(fluent = true)
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Values {

    @NonNull
    @NotNull
    private final Kombiner origin;

    @NotNull
    final Set<Q<?>> issuedKeys = new HashSet<>();

    @NotNull
    final Map<Q<?>, ? super Object> cache = new HashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes", "RedundantCast"})
    <U> K<U> k(@NotNull @NonNull final String key,
               @Nullable final Q<U> type) {
        this.issue(key, type);
        return new Kombiner_K<>(this.origin, key, type == null ? (Q) Q._VOID : type);
    }

    @Contract(mutates = "this")
    @Nullable
    @SuppressWarnings("unchecked")
    <U> U v(@NonNull @NotNull final String key,
            @Nullable final Q<?> type,
            @Nullable final U def,
            final boolean mustExist) {
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

    boolean has(@NonNull @NotNull final Q<?> t) {
        Objects.requireNonNull(t.key());
        return this.cache.containsKey(t);
    }

    private void issue(@NotNull @NonNull final String key,
                       @Nullable final Q<?> q) {
        this.issuedKeys.add(Q.withKey0(q, key));
    }


    @NotNull
    Map<Q<?>, Object> copy() {
        return new HashMap<>(this.cache);
    }

    Kombiner_Values replace(@NotNull @NonNull final Map<Q<?>, Object> copy) {
        this.cache.clear();
        this.cache.putAll(copy);
        return this;
    }

    void origForEach(Consumer<Q<?>> action) {
        this.issuedKeys.forEach(action);
    }

}
