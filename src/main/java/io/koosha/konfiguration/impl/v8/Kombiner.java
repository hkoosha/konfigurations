package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.KfgIllegalArgumentException;
import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Almost Thread-safe, <b>NOT</b> immutable.
 */
@ThreadSafe
@ApiStatus.Internal
final class Kombiner implements Konfiguration {

    @NotNull
    private final String name;

    @NotNull final Kombiner_Sources sources;

    @NotNull final Kombiner_Lock _lock;

    @NotNull final Kombiner_Observers observers;

    @NotNull final Kombiner_Values values;

    @Nullable
    private volatile KonfigurationManager man;

    Kombiner(@NotNull final String name,
             @NotNull final Collection<Konfiguration> sources,
             @Nullable final Long lockWaitTimeMillis,
             final boolean fairLock) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(sources, "sources");

        this.name = name;

        final Map<Handle, Konfiguration> newSources = new HashMap<>();
        sources.stream()
               .peek(source -> {
                   if (source == null)
                       throw new KfgIllegalArgumentException(
                               name, "null in config sources");
                   if (source instanceof SubsetView)
                       throw new KfgIllegalArgumentException(
                               name, "can not kombine a " + source.getClass().getName() + " konfiguration.");
               })
               .flatMap(source ->// Unwrap.
                                source instanceof Kombiner
                                        ? ((Kombiner) source).sources.stream()
                                        : Stream.of(source))
               .forEach(source -> newSources.put(new HandleImpl(), source));
        if (newSources.isEmpty())
            throw new KfgIllegalArgumentException(name, "no source given");

        this._lock = new Kombiner_Lock(name, lockWaitTimeMillis, fairLock);
        this.observers = new Kombiner_Observers(this.name);
        this.man = new Kombiner_Manager(this);
        this.values = new Kombiner_Values(this);
        this.sources = new Kombiner_Sources(this);

        this.sources.replace(newSources);
    }

    @Contract(pure = true)
    Kombiner_Lock lock() {
        return this._lock;
    }

    <T> T r(@NotNull final Supplier<T> func) {
        Objects.requireNonNull(func, "func");
        return lock().doReadLocked(func);
    }

    <T> T w(@NotNull final Supplier<T> func) {
        Objects.requireNonNull(func, "func");
        return lock().doWriteLocked(func);
    }

    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    @NotNull
    public String name() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    @NotNull
    public KonfigurationManager manager() {
        return w(() -> {
            final KonfigurationManager m = this.man;
            if (m == null)
                throw new KfgIllegalStateException(this.name(), null, null, null, "manager is already taken out");
            this.man = null;
            return m;
        });
    }

    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Boolean> bool(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, Kind.BOOL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Byte> byte_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, Kind.BYTE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Character> char_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, Kind.CHAR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Short> short_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, Kind.SHORT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Integer> int_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, Kind.INT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Long> long_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, Kind.LONG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Float> float_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, Kind.FLOAT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Double> double_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, Kind.DOUBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<String> string(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, Kind.STRING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<List<U>> list(@NotNull final String key,
                               @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, type.asList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<Set<U>> set(@NotNull final String key,
                             @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, type.asSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<U> custom(@NotNull final String key,
                           @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return this.values.k(key, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull final String key,
                       @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(key, "key");
        final Kind<?> t = type.withKey(key);
        return r(() -> this.values.has(t) || this.sources.has(key, type));
    }

    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Handle registerSoft(@NotNull final KeyObserver observer,
                               @Nullable final String key) {
        Objects.requireNonNull(observer, "observer");
        return w(() -> observers.registerSoft(observer, key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Handle register(@NotNull final KeyObserver observer,
                           @NotNull final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");
        return w(() -> observers.registerHard(observer, key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deregister(@NotNull final Handle observer,
                           @NotNull final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");
        w(() -> {
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
    public final Konfiguration subset(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return new SubsetView(this.name() + "::" + key, this, key);
    }

}
