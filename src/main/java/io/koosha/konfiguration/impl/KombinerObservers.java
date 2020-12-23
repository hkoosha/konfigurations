package io.koosha.konfiguration.impl;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KeyObserver;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@NotThreadSafe
@ApiStatus.Internal
final class KombinerObservers {

    private final Kombiner origin;
    private final LinkedHashMap<Handle, Observer> observers = new LinkedHashMap<>();

    KombinerObservers(@NotNull final Kombiner origin) {
        Objects.requireNonNull(origin, "origin");
        this.origin = origin;
    }

    @NotNull
    Handle registerSoft(@NotNull final KeyObserver observer,
                        @NotNull final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");

        final Observer o = new Observer(new WeakReference<>(observer), key);
        this.origin.w(() -> this.observers.put(o.handle(), o));
        return o.handle();
    }

    @NotNull
    Handle registerHard(@NotNull final KeyObserver observer,
                        @NotNull final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");

        final Observer o = new Observer(observer, key);
        this.origin.w(() -> this.observers.put(o.handle(), o));
        return o.handle();
    }

    void remove(@NotNull final Handle handle) {
        Objects.requireNonNull(handle, "handle");
        this.origin.w(() -> this.observers.remove(handle));
    }

    void deregister(@NotNull final Handle handle,
                    @NotNull final String key) {
        Objects.requireNonNull(handle, "handle");
        Objects.requireNonNull(key, "key");

        this.origin.w(() -> {
            final Observer o = this.observers.get(handle);
            if (o != null)
                o.remove(key);
            return null;
        });
    }

    @NotNull
    Collection<Runnable> getKeyListeners(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.observers
            .values()
            .stream()
            .filter(it -> it.has(key))
            .map(Observer::listener)
            .filter(Objects::nonNull)
            .map(it -> (Runnable) () -> it.accept(key))
            .collect(Collectors.toList());
    }

    @NotThreadSafe
    private static final class Observer {

        private final Handle handle = new HandleImpl();

        @Nullable
        private final WeakReference<KeyObserver> soft;

        @Nullable
        private final KeyObserver hard;

        private final Set<String> interestedKeys = new HashSet<>();

        Observer(@NotNull final WeakReference<KeyObserver> keyObserver,
                 @NotNull final String... keys) {
            Objects.requireNonNull(keyObserver, "keyObserver");
            this.soft = keyObserver;
            this.hard = null;
            for (final String key : keys)
                this.add(key);
        }

        Observer(@NotNull final KeyObserver keyObserver,
                 @NotNull final String... keys) {
            Objects.requireNonNull(keyObserver, "keyObserver");
            this.soft = null;
            this.hard = keyObserver;
            for (final String key : keys)
                this.add(key);
        }

        @Nullable
        @Contract(pure = true)
        KeyObserver listener() {
            return this.soft != null
                ? this.soft.get()
                : this.hard;
        }

        void add(@NotNull final String key) {
            Objects.requireNonNull(key, "key");
            this.interestedKeys.add(key);
        }

        void remove(@NotNull final String key) {
            Objects.requireNonNull(key, "key");
            this.interestedKeys.remove(key);
        }

        boolean has(@NotNull final String key) {
            Objects.requireNonNull(key, "key");
            return this.interestedKeys.contains(key);
        }


        public Handle handle() {
            return this.handle;
        }


        @Override
        public boolean equals(final Object o) {
            return o == this || o instanceof Observer
                && Objects.equals(this.handle, ((Observer) o).handle);
        }

        @Override
        public int hashCode() {
            return 59 * this.handle.hashCode();
        }

    }

}
