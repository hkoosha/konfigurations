package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KeyObserver;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Accessors(fluent = true)
@EqualsAndHashCode(of = "handle")
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Observer {


    @Getter
    private final Handle handle = new HandleImpl();

    @Nullable
    private final WeakReference<KeyObserver> soft;

    private final Set<String> interestedKeys = new HashSet<>();

    @Nullable
    private final KeyObserver hard;

    Kombiner_Observer(@NonNull @NotNull final WeakReference<KeyObserver> soft) {
        this.soft = soft;
        this.hard = null;
    }

    Kombiner_Observer(@NonNull @NotNull final KeyObserver hard) {
        this.soft = null;
        this.hard = hard;
    }

    @Nullable
    @Contract(pure = true)
    KeyObserver listener() {
        if (this.soft != null)
            return this.soft.get();
        else
            return this.hard;
    }

    void add(@NotNull @NonNull final String key) {
        this.interestedKeys.add(key);
    }

    void remove(@NotNull @NonNull final String key) {
        this.interestedKeys.remove(key);
    }

    boolean has(@NotNull @NonNull final String key) {
        return this.interestedKeys.contains(key);
    }

}
