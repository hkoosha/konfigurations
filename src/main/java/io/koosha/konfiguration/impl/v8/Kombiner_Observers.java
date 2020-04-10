package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KeyObserver;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Observers {

    @NotNull
    private final String name;

    private final Map<Handle, Kombiner_Observer> observers = new LinkedHashMap<>();

    Kombiner_Observers(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        this.name = name;
    }

    @NotNull
    Handle registerSoft(@NotNull final KeyObserver observer,
                        @Nullable final String key) {
        Objects.requireNonNull(observer, "observer");
        final Kombiner_Observer o = new Kombiner_Observer(new WeakReference<>(observer));
        this.observers.put(o.handle(), o);
        return o.handle();
    }

    @NotNull
    Handle registerHard(@NotNull final KeyObserver observer,
                        @Nullable final String key) {
        Objects.requireNonNull(observer, "observer");
        final Kombiner_Observer o = new Kombiner_Observer(observer);
        this.observers.put(o.handle(), o);
        return o.handle();
    }

    void remove(@NotNull final Handle handle) {
        Objects.requireNonNull(handle, "handle");
        this.observers.remove(handle);
    }

    void deregister(@NotNull final Handle handle,
                    @NotNull final String key) {
        Objects.requireNonNull(handle, "handle");
        Objects.requireNonNull(key, "key");
        final Kombiner_Observer o = this.observers.get(handle);
        if (o != null)
            o.remove(key);
    }

    @NotNull
    Collection<Runnable> get(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.observers
                .values()
                .stream()
                .filter(x -> x.has(key))
                .map(Kombiner_Observer::listener)
                .filter(Objects::nonNull)
                .map(x -> (Runnable) () -> x.accept(key))
                .collect(toList());
    }

}
