package io.koosha.konfiguration;


import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;


/**
 * Konfiguration observer which observes change in the konfiguration source.
 * <p>
 * This observer only receives the key. This means if you want to listen to
 * change in multiple sources and differentiate between them, you need to use
 * intermediate objects, one listener for each source, yourself.
 */
@ApiStatus.AvailableSince(Factory.VERSION_1)
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
    @Contract(mutates = "this",
            value = "_ -> _")
    void accept(@NotNull String key);


    /**
     * Don't forget to have a proper hashcode which works with {@link java.util.Map}.
     * <p>
     * {@link Object#hashCode()}'s default implementation would suffice, but
     * if you've overridden that, keep {@link java.util.Map} in mind.
     *
     * @return hashCode.
     */
    @Override
    int hashCode();

    /**
     * Don't forget to have a proper equals which works with {@link java.util.Map}.
     * <p>
     * {@link Object#equals(Object)}'s default implementation would suffice,
     * but if you've overridden that, keep {@link java.util.Map} in mind.
     *
     * @return true if equals..
     */
    @Override
    boolean equals(Object obj);

}
