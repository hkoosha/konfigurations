package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Manager implements KonfigurationManager {

    @NotNull
    private final Kombiner origin;

    public Kombiner_Manager(@NotNull final Kombiner kombiner) {
        Objects.requireNonNull(kombiner, "kombiner");
        this.origin = kombiner;
    }


    /**
     * {@inheritDoc}
     *
     * @return
     */
    @NotNull
    @Override
    public Map<String, Collection<Runnable>> update() {
        return origin.r(this::update0);
    }


    public boolean hasUpdate() {
        return origin.r(this::hasUpdate0);
    }

    private boolean hasUpdate0() {
        return origin
                .sources
                .stream()
                .anyMatch(x -> ((Source) x).hasUpdate());
    }

    private Map<String, Collection<Runnable>> update0() {
        if (!this.hasUpdate0())
            return emptyMap();

        final Map<Handle, Konfiguration> newSources = origin.sources.copy();
        newSources.entrySet().forEach(x -> x.setValue(
                x.getValue() instanceof Source
                        ? ((Source) x.getValue()).updatedCopy()
                        : x.getValue()
        ));

        final Set<Kind<?>> updated = new HashSet<>();
        final Map<Kind<?>, Object> newCache = origin.values.copy();
        origin.values.issuedKeys.forEach(q -> {
            final String key = requireNonNull(q.key(), "ket passed through kombiner is null");

            final Optional<Konfiguration> first = newSources
                    .values()
                    .stream()
                    .filter(x -> x.has(key, q))
                    .findFirst();

            final Object newV = first
                    .map(konfiguration -> konfiguration.custom(q.key(), q))
                    .orElse(null);

            final Object oldV = origin.has(q.key(), q)
                    ? origin.values.v_(q)
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
                    .stream()
                    // External non-optimizable konfig sources.
                    .filter(target -> !(target instanceof Source))
                    .map(Konfiguration::manager)
                    .map(KonfigurationManager::update)
                    .peek(x -> x.entrySet().forEach(e -> e.setValue(
                            // just to wrap!
                            e.getValue().stream().map(r -> {
                                Objects.requireNonNull(r, "runnable");
                                // We can not be sure if given runnable is safe
                                // to be put in a map // So we create a plain
                                // object wrapping it.
                                //noinspection Convert2Lambda,Anonymous2MethodRef
                                return new Runnable() {
                                    @Override
                                    public void run() {
                                        r.run();
                                    }
                                };
                            })
                             .collect(toList())
                    )))
                    .reduce(new HashMap<>(), (m0, m1) -> {
                        m1.forEach((m1k, m1c) -> m0.computeIfAbsent(
                                m1k, m1k_ -> new ArrayList<>()).addAll(m1c));
                        return m0;
                    });

            for (final Kind<?> kind : updated)
                //noinspection ConstantConditions
                result.computeIfAbsent(kind.key(), (q_) -> new ArrayList<>())
                      .addAll(this.origin.observers.get(kind.key()));

            origin.sources.replace(newSources);
            origin.values.replace(newCache);

            return result;
        });
    }

}
