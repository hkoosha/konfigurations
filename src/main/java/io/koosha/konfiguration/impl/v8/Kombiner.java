package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.KfgIllegalArgumentException;
import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.SubsetView;
import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Almost Thread-safe, <b>NOT</b> immutable.
 */
@ApiStatus.Internal
final class Kombiner implements Konfiguration {

    @NotNull
    private final String name;

    @NotNull
    final KombinerSources sources;

    @NotNull
    final KombinerLock lock;

    @NotNull
    final KombinerObservers observers;

    @Nullable
    private volatile KonfigurationManager man;

    final Set<Kind<?>> issuedKeys = new HashSet<>();
    private final Map<Kind<?>, ? super Object> cache = new HashMap<>();

    final boolean updatable;

    private LinkedHashMap<Handle, Source> unwrap(@NotNull final Collection<Konfiguration> sources) {
        Objects.requireNonNull(sources, "sources");

        final LinkedHashMap<Handle, Source> newSources = new LinkedHashMap<>();
        sources.stream()
               .flatMap(source ->
                   source instanceof Kombiner
                       ? ((Kombiner) source).sources.sources().stream()
                       : Stream.of(source))
               .peek(source -> Objects.requireNonNull(source, "null in config sources"))
               .peek(source -> {
                   if (source instanceof SubsetView)
                       throw new KfgIllegalArgumentException(
                           name, "can not kombine a " + source.getClass().getName() + " konfiguration.");
               })
               .peek(source -> {
                   if (!(source instanceof Source))
                       throw new KfgIllegalArgumentException(
                           name, "can not kombine a " + source.getClass().getName() + " konfiguration, only" +
                           " " + Source.class.getName() + " can be accepted.");
               })
               .forEach(source -> newSources.put(new HandleImpl(), (Source) source));

        return newSources;
    }

    Kombiner(@NotNull final String name,
             @NotNull final Collection<Konfiguration> sources,
             @Nullable final Long lockWaitTimeMillis,
             final boolean fairLock,
             final boolean updatable) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(sources, "sources");

        this.name = name;
        this.updatable = updatable;

        final LinkedHashMap<Handle, Source> newSources = this.unwrap(sources);
        if (newSources.isEmpty())
            throw new KfgIllegalArgumentException(name, "no source given");

        this.lock = new KombinerLock(name, lockWaitTimeMillis, fairLock);
        this.observers = new KombinerObservers(this);
        this.man = new KombinerManager(this);
        this.sources = new KombinerSources();

        this.sources.replaceSources(newSources);
    }

    // =========================================================================

    <T> T r(@NotNull final Supplier<T> func) {
        Objects.requireNonNull(func, "func");
        return lock.doReadLocked(func);
    }

    <T> T w(@NotNull final Supplier<T> func) {
        Objects.requireNonNull(func, "func");
        return lock.doWriteLocked(func);
    }

    <U> K<U> k(@NotNull final String key,
               @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final Kind<U> withKey = type.withKey(key);

        final boolean contains = this.r(() -> this.issuedKeys.contains(withKey));

        if (!contains)
            this.w(() -> {
                this.issuedKeys.add(withKey);
                return null;
            });

        return new KombinerK<>(this, key, type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    <U> U getCachedValueOrIssueIt(@NotNull final String key,
                                  @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final Kind<U> t = type.withKey(key);

        final AtomicBoolean hadValue = new AtomicBoolean(false);
        final U value = this.r(() -> {
            if (cache.containsKey(t)) {
                hadValue.set(true);
                return (U) cache.get(t);
            }
            return null;
        });
        if (hadValue.get())
            return value;

        return this.w(() -> cache.containsKey(t)
            ? (U) cache.get(t)
            : (U) this.issueValue(t));
    }

    @SuppressWarnings("unchecked")
    <U> Optional<U> getCachedValue(@NotNull final String key,
                                   @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final Kind<U> t = type.withKey(key);

        return cache.containsKey(t)
            ? Optional.ofNullable((U) cache.get(t))
            : Optional.empty();
    }

    @Nullable
    Object issueValue(@NotNull final Kind<?> key) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(key.key(), "key's text key");

        final Optional<Source> find = this
            .sources
            .sources()
            .stream()
            .filter(source -> source.has(key.key(), key))
            .findFirst();

        if (!find.isPresent())
            throw new KfgMissingKeyException(this.name(), key.key(), key);

        this.issuedKeys.add(key.withKey(key.key()));
        final Object value = find.get().custom(key.key(), key).v();
        this.cache.put(key, value);
        return value;
    }

    boolean hasInCache(@NotNull final Kind<?> key) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(key.key(), "key's text key");
        return this.cache.containsKey(key);
    }

    @NotNull
    Map<Kind<?>, Object> cacheCopy() {
        return new HashMap<>(this.cache);
    }

    void replaceCache(@NotNull final Map<Kind<?>, Object> copy) {
        Objects.requireNonNull(copy, "copy");

        if (!this.updatable)
            throw new KfgIllegalStateException(this.name, "konfiguration is not updatable");

        this.cache.clear();
        this.cache.putAll(copy);
    }

    // =========================================================================

    @Override
    @Contract(pure = true)
    @NotNull
    public String name() {
        return this.name;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public Optional<KonfigurationManager> manager() {
        return this.w(() -> {
            final KonfigurationManager m = this.man;
            this.man = null;
            return Optional.ofNullable(m);
        });
    }

    @Override
    @NotNull
    public K<Boolean> bool(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.k(key, Kind.BOOL);
    }

    @Override
    @NotNull
    public K<Byte> byte_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.k(key, Kind.BYTE);
    }

    @Override
    @NotNull
    public K<Character> char_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.k(key, Kind.CHAR);
    }

    @Override
    @NotNull
    public K<Short> short_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.k(key, Kind.SHORT);
    }

    @Override
    @NotNull
    public K<Integer> int_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.k(key, Kind.INT);
    }

    @Override
    @NotNull
    public K<Long> long_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.k(key, Kind.LONG);
    }

    @Override
    @NotNull
    public K<Float> float_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.k(key, Kind.FLOAT);
    }

    @Override
    @NotNull
    public K<Double> double_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.k(key, Kind.DOUBLE);
    }

    @Override
    @NotNull
    public K<String> string(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.k(key, Kind.STRING);
    }

    @Override
    @NotNull
    public <U> K<List<U>> list(@NotNull final String key,
                               @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        return this.k(key, type.asList());
    }

    @Override
    @NotNull
    public <U> K<Set<U>> set(@NotNull final String key,
                             @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        return this.k(key, type.asSet());
    }

    @Override
    @NotNull
    public <U> K<U> custom(@NotNull final String key,
                           @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        return this.k(key, type);
    }

    @Override
    public boolean has(@NotNull final String key,
                       @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final Kind<?> t = type.withKey(key);
        return this.r(() -> this.hasInCache(t) || this.sources.has(key, type));
    }

    @NotNull
    @Override
    public Handle registerSoft(@NotNull final KeyObserver observer,
                               @NotNull final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");

        if (!this.updatable)
            return HandleImpl.NONE;

        return observers.registerSoft(observer, key);
    }

    @Override
    @NotNull
    public Handle register(@NotNull final KeyObserver observer,
                           @NotNull final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");

        if (!this.updatable)
            return HandleImpl.NONE;

        return observers.registerHard(observer, key);
    }

    @Override
    public void deregister(@NotNull final Handle observer,
                           @NotNull final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");

        if (!this.updatable)
            return;

        if (Objects.equals(KeyObserver.LISTEN_TO_ALL, key))
            this.observers.remove(observer);
        else
            this.observers.deregister(observer, key);
    }

    @Override
    @NotNull
    public final Konfiguration subset(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return new SubsetView(this.name() + "::" + key, this, key);
    }

}
