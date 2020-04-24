package io.koosha.konfiguration;

import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Map;

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
        boolean any = false;
        for (final Map.Entry<String, Collection<Runnable>> entry : this.update().entrySet()) {
            entry.getValue().forEach(Runnable::run);
            any = true;
        }
        return any;
    }

    boolean hasUpdate();

}
