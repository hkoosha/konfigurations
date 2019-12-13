package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Almost Thread-safe, <b>NOT</b> immutable.
 */
@ThreadSafe
@ApiStatus.Internal
final class Kombiner implements Konfiguration {

    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    @NotNull
    final Kombiner_Sources sources;

    @NotNull
    final Kombiner_Lock _lock;

    @NotNull
    final Kombiner_Observers observers;

    @NotNull
    final Kombiner_Values values;

    @Nullable
    private volatile KonfigurationManager man;

    Kombiner(@NotNull @NonNull final String name,
             @NotNull @NonNull final Collection<Konfiguration> sources,
             @Nullable final Long lockWaitTimeMillis,
             final boolean fairLock) {
        this.name = name;

        final Map<Handle, Konfiguration> s = new HashMap<>();
        sources.stream()
               .peek(k -> {
                   if (k == null)
                       throw new KfgIllegalArgumentException(name, "null in config sources");
                   if (k instanceof JSubsetView)
                       throw new KfgIllegalArgumentException(name, "Can not kombine a " + k.getClass().getName() + " konfiguration.");
               })
               .flatMap(k ->// Unwrap.
                       k instanceof Kombiner
                       ? ((Kombiner) k).sources.vs()
                       : Stream.of(k))
               .forEach(x -> s.put(new HandleImpl(), x));
        if (s.isEmpty())
            throw new KfgIllegalArgumentException(name, "no source given");

        this._lock = new Kombiner_Lock(name, lockWaitTimeMillis, fairLock);
        this.observers = new Kombiner_Observers(this.name);
        this.man = new Kombiner_Manager(this);
        this.values = new Kombiner_Values(this);
        this.sources = new Kombiner_Sources(this);

        this.sources.replace(s);
    }

    Kombiner_Lock lock() {
        if (this.man != null)
            throw new KfgIllegalStateException(this.name(), "konfiguration manager is not taken out yet");
        return this._lock;
    }

    <T> T r(@NonNull @NotNull final Supplier<T> func) {
        return lock().doReadLocked(func);
    }

    <T> T w(@NonNull @NotNull final Supplier<T> func) {
        return lock().doWriteLocked(func);
    }

    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public KonfigurationManager manager() {
        return w(() -> {
            final KonfigurationManager m = this.man;
            this.man = null;
            if (m == null)
                throw new KfgIllegalStateException(this.name(), null, null, null, "manager is already taken out");
            return m;
        });
    }

    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Boolean> bool(@NotNull @NonNull final String key) {
        return this.values.k(key, Q.BOOL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Byte> byte_(@NotNull @NonNull final String key) {
        return this.values.k(key, Q.BYTE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Character> char_(@NotNull @NonNull final String key) {
        return this.values.k(key, Q.CHAR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Short> short_(@NotNull @NonNull final String key) {
        return this.values.k(key, Q.SHORT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Integer> int_(@NotNull @NonNull final String key) {
        return this.values.k(key, Q.INT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Long> long_(@NotNull @NonNull final String key) {
        return this.values.k(key, Q.LONG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Float> float_(@NotNull @NonNull final String key) {
        return this.values.k(key, Q.FLOAT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Double> double_(@NotNull @NonNull final String key) {
        return this.values.k(key, Q.DOUBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<String> string(@NotNull @NonNull final String key) {
        return this.values.k(key, Q.STRING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<List<U>> list(@NotNull @NonNull final String key,
                               @Nullable final Q<List<U>> type) {
        return this.values.k(key, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U, V> K<Map<U, V>> map(@NotNull @NonNull final String key,
                                   @Nullable final Q<Map<U, V>> type) {
        return this.values.k(key, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<Set<U>> set(@NotNull @NonNull final String key,
                             @Nullable final Q<Set<U>> type) {
        return this.values.k(key, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<U> custom(@NonNull final String key,
                           @Nullable final Q<U> type) {
        return this.values.k(key, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull @NonNull final String key,
                       @Nullable final Q<?> type) {
        final Q<?> t = Q.withKey0(type, key);
        return r(() -> this.values.has(t) || this.sources.has(key, type));
    }

    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Handle registerSoft(@NonNull @NotNull final KeyObserver observer,
                               @Nullable final String key) {
        return w(() -> observers.registerSoft(observer, key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Handle register(@NotNull @NonNull final KeyObserver observer,
                           @NotNull @NonNull final String key) {
        return w(() -> observers.registerHard(observer, key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Konfiguration deregister(@NotNull @NonNull final Handle observer,
                                    @NotNull @NonNull final String key) {
        return w(() -> {
            if (Objects.equals(KeyObserver.LISTEN_TO_ALL, key))
                this.observers.remove(observer);
            else
                this.observers.deregister(observer, key);
            return this;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final Konfiguration subset(@NonNull @NotNull final String key) {
        this.lock();
        return new JSubsetView(this.name() + "::" + key, this, key);
    }

}
