package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.Q;
import lombok.NonNull;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Manager implements KonfigurationManager {

    @NotNull
    @NonNull
    private final Kombiner origin;

    private final AtomicReference<Kombiner> kombiner;

    public Kombiner_Manager(@NotNull @NonNull Kombiner kombiner) {
        this.origin = kombiner;
        this.kombiner = new AtomicReference<>(kombiner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Konfiguration getAndSetToNull() {
        return this.kombiner.getAndSet(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasUpdate() {
        if (this.kombiner.get() != null)
            throw new IllegalStateException("getAndSetToNull() not called yet");
        return origin.r(this::hasUpdate0);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @NotNull
    @Override
    public Map<String, Collection<Runnable>> update() {
        if (this.kombiner.get() != null)
            throw new IllegalStateException("getAndSetToNull() not called yet");
        return origin.r(this::update0);
    }

    private boolean hasUpdate0() {
        return origin
                .sources
                .vs()
                .map(Konfiguration::manager)
                .anyMatch(KonfigurationManager::hasUpdate);
    }

    private Map<String, Collection<Runnable>> update0() {
        if (!this.hasUpdate0())
            return emptyMap();

        final Map<Handle, Konfiguration> newSources = origin.sources.copy();
        newSources.entrySet().forEach(x -> x.setValue(
                x.getValue() instanceof Konfiguration0
                ? ((Konfiguration0) x.getValue()).manager()._update()
                : x.getValue()
        ));

        final Set<Q<?>> updated = new HashSet<>();
        final Map<Q<?>, Object> newCache = origin.values.copy();
        origin.values.origForEach(q -> {
            final String key = requireNonNull(q.key(), "ket passed through kombiner is null");

            final Optional<Konfiguration> first = newSources
                    .values()
                    .stream()
                    .filter(x -> x.has(key, q))
                    .findFirst();

            final Object newV = first.map(konfiguration ->
                    konfiguration.custom(q.key(), q)).orElse(null);

            final Object oldV =
                    origin.has(q.key(), q)
                    ? origin.values.v_(q, null, true)
                    : null;

            // Went missing or came into existence.
            if (origin.values.has(q) != first.isPresent()
                    || !Objects.equals(newV, oldV))
                updated.add(q);

            if (first.isPresent())
                newCache.put(q, newV);
        });

        return origin.w(() -> {
            final Map<String, Collection<Runnable>> result = origin
                    .sources
                    .vs()
                    // External non-optimizable konfig sources.
                    .filter(not(Konfiguration0.class::isInstance))
                    .map(Konfiguration::manager)
                    .map(KonfigurationManager::update)
                    .peek(x -> x.entrySet().forEach(e -> e.setValue(
                            // just to wrap!
                            e.getValue().stream().map(Kombiner_Manager::wrap)
                             .collect(toList())
                    )))
                    .reduce(new HashMap<>(), (m0, m1) -> {
                        m1.forEach((m1k, m1c) -> m0.computeIfAbsent(
                                m1k, m1k_ -> new ArrayList<>()).addAll(m1c));
                        return m0;
                    });

            for (final Q<?> q : updated)
                //noinspection ConstantConditions
                result.computeIfAbsent(q.key(), (q_) -> new ArrayList<>())
                      .addAll(this.origin.observers.get(q.key()));

            origin.sources.replace(newSources);
            origin.values.replace(newCache);

            return result;
        });
    }

    private static <T> Predicate<T> not(@NotNull @NonNull final Predicate<? super T> target) {
        //noinspection unchecked
        return (Predicate<T>) target.negate();
    }

    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
    private static Runnable wrap(@NonNull @NotNull final Runnable r) {
        // We can not be sure if given runnable is safe to be put in a map
        // So we create a plain object wrapping it.
        return new Runnable() {
            @Override
            public void run() {
                r.run();
            }
        };
    }

}
