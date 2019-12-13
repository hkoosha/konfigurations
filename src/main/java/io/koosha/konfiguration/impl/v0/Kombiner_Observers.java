package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KeyObserver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Observers {

    @NotNull
    @NonNull
    private final String name;

    private final Map<Handle, Kombiner_Observer> observers = new LinkedHashMap<>();

    @NotNull
    Handle registerSoft(@NonNull @NotNull final KeyObserver observer,
                        @Nullable final String key) {
        final Kombiner_Observer o = new Kombiner_Observer(new WeakReference<>(observer));
        this.observers.put(o.handle(), o);
        return o.handle();
    }

    @NotNull
    Handle registerHard(@NonNull @NotNull final KeyObserver observer,
                        @Nullable final String key) {
        final Kombiner_Observer o = new Kombiner_Observer(observer);
        this.observers.put(o.handle(), o);
        return o.handle();
    }

    void remove(@NonNull @NotNull final Handle handle) {
        this.observers.remove(handle);
    }

    void deregister(@NonNull @NotNull final Handle handle,
                    @NonNull @NotNull final String key) {
        final Kombiner_Observer o = this.observers.get(handle);
        if (o != null)
            o.remove(key);
    }

    @NotNull
    Collection<Runnable> get(@NonNull @NotNull final String key) {
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
