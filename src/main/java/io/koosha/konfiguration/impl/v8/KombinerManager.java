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
import java.util.Collections;
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
final class KombinerManager implements KonfigurationManager {

    @NotNull
    private final Kombiner origin;

    public KombinerManager(@NotNull final Kombiner kombiner) {
        Objects.requireNonNull(kombiner, "kombiner");
        this.origin = kombiner;
    }

    @NotNull
    @Override
    public Map<String, Collection<Runnable>> update() {
        if(!this.origin.updatable)
            return Collections.emptyMap();

        final Map<Handle, Konfiguration> newSources = new HashMap<>();
        final Set<Kind<?>> updated = new HashSet<>();
        final Map<Kind<?>, Object> newCache = new HashMap<>();

        final Map<String, Collection<Runnable>> ret = this.origin.r(() -> {
            if (this.origin.sources
                .sourcesStream()
                .noneMatch(x -> ((Source) x).hasUpdate()))
                return emptyMap();


            newSources.putAll(this.origin.sources.sourcesCopy());
            newSources.entrySet().forEach(x -> x.setValue(((Source) x.getValue()).updatedCopy()));

            newCache.putAll(this.origin.cacheCopy());

            this.origin.issuedKeys.forEach(q -> {
                final String key = requireNonNull(
                    q.key(), "ket passed through kombiner is null");

                final Optional<Konfiguration> first = newSources
                    .values()
                    .stream()
                    .filter(x -> x.has(key, q))
                    .findFirst();

                final Object newV = first
                    .map(konfiguration -> konfiguration.custom(q.key(), q).v())
                    .orElse(null);

                final Object oldV = this.origin.has(q.key(), q)
                    ? this.origin.issueValue(q)
                    : null;

                // Went missing or came into existence.
                if (this.origin.hasInCache(q) != first.isPresent()
                    || !Objects.equals(newV, oldV))
                    updated.add(q);

                if (first.isPresent())
                    newCache.put(q, newV);
            });

            return null;
        });
        if (ret != null)
            return ret;

        return this.origin.w(() -> {
            //noinspection OptionalGetWithoutIsPresent
            final Map<String, Collection<Runnable>> result = this
                .origin
                .sources
                .sourcesStream()
                // External non-optimizable konfig sources.
                .filter(target -> !(target instanceof Source))
                .map(Konfiguration::manager)
                .map(it -> it.get().update())
                .peek(x -> x.entrySet().forEach(e -> e.setValue(
                    // just to wrap!
                    e.getValue().stream().map(r -> {
                        requireNonNull(r, "ret");
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

            this.origin.sources.replaceSources(newSources);
            this.origin.replaceCache(newCache);

            return result;
        });
    }

    @Override
    public boolean hasUpdate() {
        if(!this.origin.updatable)
            return false;

        return this.origin.r(() -> this
            .origin
            .sources
            .sourcesStream()
            .anyMatch(x -> ((Source) x).hasUpdate()));
    }

}
