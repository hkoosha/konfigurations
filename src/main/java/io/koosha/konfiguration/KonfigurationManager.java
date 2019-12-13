package io.koosha.konfiguration;

import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

@NotThreadSafe
@ApiStatus.AvailableSince(Factory.VERSION_8)
public interface KonfigurationManager {

    /**
     * Indicates whether if anything is actually updated in the origin of
     * this source.
     *
     * <p>This action must <b>NOT</b> modify the instance.</p>
     *
     * <p><b>VERY VERY IMPORTANT:</b> This method is to be called only from
     * a single thread or concurrency issues will arise.</p>
     *
     * <p>Why? To check and see if it's updatable, a source might ask it's
     * origin (a web url?) to get the new content, to compare with the old
     * content, and it asks it's origin for the new content once more, to
     * actually update the values. If this method is called during
     * KonfigurationKombiner is also calling it, this might interfere and
     * lost updates may happen.</p>
     *
     * <p>To help blocking issues, update() is allowed to block the current
     * thread, and update observers will continue to work in their own
     * thread. This mechanism also helps to notify them only after when
     * <em>all</em> the combined sources are updated.</p>
     *
     * <p>NOT Thread-safe.
     *
     * @return true if the source obtained via {@link #update()} ()} will
     * differ from this source.
     */
    @Contract(pure = true)
    boolean hasUpdate();

    /**
     * Updates the source konfiguration, but does NOT call the observers.
     *
     * <p><b>NOT</b> Thread-safe
     *
     * @return list of observers and the key they should be notified about.
     */
    @NotNull
    @Contract(mutates = "this")
    Map<String, Collection<Runnable>> update();

    default void updateNow() {
        this.update().forEach((key, observers) ->
                observers.forEach(Runnable::run));
    }

    @Nullable
    Konfiguration getAndSetToNull();

}
