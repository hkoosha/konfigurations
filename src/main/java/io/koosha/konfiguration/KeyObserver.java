package io.koosha.konfiguration;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Konfiguration observer which observes change in the konfiguration source.
 * <p>
 * This observer only receives the key. This means if you want to listen to
 * change in multiple sources and differentiate between them, you need to use
 * intermediate objects, one listener for each source, yourself.
 */
@FunctionalInterface
public interface KeyObserver extends Consumer<String> {

    /**
     * Listen to this key to listen to all changes.
     */
    String LISTEN_TO_ALL = "*";


    /**
     * Called when the konfiguration for the {@code key} is changed (updated).
     *
     * @param key the konfiguration key that it's value was updated. null means
     *            update to some key. Observers registering to null, will
     *            reachieved one call to {@code accept(null)} for each update.
     *            (not for each key being updated, but an update for each cycle
     *            of updates).
     */
    void accept(@NotNull String key);

}
