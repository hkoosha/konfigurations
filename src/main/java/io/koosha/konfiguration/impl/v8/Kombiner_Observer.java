package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KeyObserver;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Observer {

    private final Handle handle = new HandleImpl();

    @Nullable
    private final WeakReference<KeyObserver> soft;

    private final Set<String> interestedKeys = new HashSet<>();

    @Nullable
    private final KeyObserver hard;

    Kombiner_Observer(@NotNull final WeakReference<KeyObserver> keyObserver) {
        Objects.requireNonNull(keyObserver, "keyObserver");
        this.soft = keyObserver;
        this.hard = null;
    }

    Kombiner_Observer(@NotNull final KeyObserver keyObserver) {
        Objects.requireNonNull(keyObserver, "keyObserver");
        this.soft = null;
        this.hard = keyObserver;
    }

    @Nullable
    @Contract(pure = true)
    KeyObserver listener() {
        if (this.soft != null)
            return this.soft.get();
        else
            return this.hard;
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
        return o == this ||
                o instanceof Kombiner_Observer
                        && Objects.equals(this.handle, ((Kombiner_Observer) o).handle);
    }

    @Override
    public int hashCode() {
        return 59 + this.handle.hashCode();
    }

}
