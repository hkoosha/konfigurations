package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.KfgAssertionException;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@NotThreadSafe
@ApiStatus.Internal
final class KombinerManager implements KonfigurationManager {

    @NotNull
    private final Kombiner origin;

    KombinerManager(@NotNull final Kombiner kombiner) {
        Objects.requireNonNull(kombiner, "kombiner");
        this.origin = kombiner;
    }

    @NotNull
    @Override
    public Map<String, Collection<Runnable>> update() {
        if (!this.origin.updatable)
            throw new KfgAssertionException(this.origin.name(), null, null, null, "update is not supported");

        final LinkedHashMap<Handle, Source> newSources = new LinkedHashMap<>();
        final Map<Kind<?>, Object> newCache = new HashMap<>();
        final Set<Kind<?>> updatedKeys = new HashSet<>();
        final Map<String, Collection<Runnable>> toBeNotifiedListeners = new HashMap<>();

        final boolean noneMatched = this.origin.r(() -> {
            if (this.origin.sources
                .sources()
                .stream()
                .noneMatch(Source::hasUpdate))
                return true;

            this.origin.sources.sourcesCopy().forEach((handle, konfiguration) ->
                newSources.put(handle, konfiguration.updatedCopy()));

            newCache.putAll(this.origin.cacheCopy());

            this.origin.issuedKeys.forEach(q -> {
                final String key = requireNonNull(
                    q.key(), "key passed through kombiner is null: " + q);

                final Optional<?> oldValue = this.origin.getCachedValue(q.key(), q);

                final Optional<Source> newValue = newSources
                    .values()
                    .stream()
                    .filter(it -> it.has(key, q))
                    .findFirst();

                final Object newValueGet = newValue.map(source -> source.custom(q.key(), q).v())
                                                   .orElse(null);

                //noinspection ConstantConditions
                if (oldValue.isPresent() != newValue.isPresent()
                    || newValue.isPresent() && oldValue.isPresent() && !Objects.equals(newValueGet, oldValue.get()))
                    updatedKeys.add(q);

                if (newValue.isPresent())
                    newCache.put(q, newValueGet);
            });

            toBeNotifiedListeners.computeIfAbsent(KeyObserver.LISTEN_TO_ALL, q_ -> new ArrayList<>())
                                 .addAll(this.origin.observers.getKeyListeners(KeyObserver.LISTEN_TO_ALL));

            for (final Kind<?> kind : updatedKeys)
                //noinspection ConstantConditions
                toBeNotifiedListeners.computeIfAbsent(kind.key(), q_ -> new ArrayList<>())
                                     .addAll(this.origin.observers.getKeyListeners(kind.key()));

            return false;
        });

        if (noneMatched)
            return Collections.emptyMap();

        this.origin.w(() -> {
            this.origin.sources.replaceSources(newSources);
            this.origin.replaceCache(newCache);
            return null;
        });

        return toBeNotifiedListeners;
    }

    @Override
    public boolean hasUpdate() {
        if (!this.origin.updatable)
            return false;

        return this
            .origin
            .sources
            .sources()
            .stream()
            .anyMatch(Source::hasUpdate);
    }

}
