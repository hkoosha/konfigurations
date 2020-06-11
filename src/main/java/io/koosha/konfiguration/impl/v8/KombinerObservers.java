package io.koosha.konfiguration.impl.v8;

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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
@NotThreadSafe
@ApiStatus.Internal
final class KombinerObservers {

    @SuppressWarnings("FieldCanBeLocal")
    @NotNull
    private final String name;

    private final Map<Handle, KombinerObserver> observers = new LinkedHashMap<>();

    KombinerObservers(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        this.name = name;
    }

    @NotNull
    Handle registerSoft(@NotNull final KeyObserver observer,
                        @Nullable final String key) {
        Objects.requireNonNull(observer, "observer");
        final KombinerObserver o = new KombinerObserver(new WeakReference<>(observer));
        this.observers.put(o.handle(), o);
        return o.handle();
    }

    @NotNull
    Handle registerHard(@NotNull final KeyObserver observer,
                        @Nullable final String key) {
        Objects.requireNonNull(observer, "observer");
        final KombinerObserver o = new KombinerObserver(observer);
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
        final KombinerObserver o = this.observers.get(handle);
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
            .map(KombinerObserver::listener)
            .filter(Objects::nonNull)
            .map(x -> (Runnable) () -> x.accept(key))
            .collect(toList());
    }

    @NotThreadSafe
    private static final class KombinerObserver {

        private final Handle handle = new HandleImpl();

        @Nullable
        private final WeakReference<KeyObserver> soft;

        private final Set<String> interestedKeys = new HashSet<>();

        @Nullable
        private final KeyObserver hard;

        KombinerObserver(@NotNull final WeakReference<KeyObserver> keyObserver) {
            Objects.requireNonNull(keyObserver, "keyObserver");
            this.soft = keyObserver;
            this.hard = null;
        }

        KombinerObserver(@NotNull final KeyObserver keyObserver) {
            Objects.requireNonNull(keyObserver, "keyObserver");
            this.soft = null;
            this.hard = keyObserver;
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
            return o == this || o instanceof KombinerObserver
                && Objects.equals(this.handle, ((KombinerObserver) o).handle);
        }

        @Override
        public int hashCode() {
            return 59 * this.handle.hashCode();
        }

    }

}
