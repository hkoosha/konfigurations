package io.koosha.konfiguration;

import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

@NotThreadSafe
@ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
public interface KonfigurationManager {

    /**
     * Updates the source konfiguration, but does NOT call the observers.
     *
     * <p><b>NOT</b> Thread-safe
     *
     * @return list of observers and the key they should be notified about.
     */
    @NotNull
    @Unmodifiable
    Map<String, Collection<Runnable>> update();

    default boolean updateNow() {
        return this.updateNow(Runnable::run);
    }

    default boolean updateNow(@NotNull final Executor executor) {
        Objects.requireNonNull(executor, "executor");

        boolean any = false;
        for (final Map.Entry<String, Collection<Runnable>> entry : this.update().entrySet()) {
            entry.getValue().forEach(executor::execute);
            any = true;
        }
        return any;
    }

    boolean hasUpdate();

}
