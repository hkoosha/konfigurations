package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * All methods are thread-safe (and should be implemented as such).
 * <p>
 * Entry point to this library is at {@link KonfigurationFactory#getInstance(String)}
 * or the versioned alternative: {@link KonfigurationFactory#getInstanceV8()}.
 */
@SuppressWarnings("unused")
@ThreadSafe
@ApiStatus.AvailableSince(KonfigurationFactory.VERSION_1)
public interface Konfiguration {

    /**
     * Always refers to latest factory.
     *
     * @return latest konfiguration factory.
     */
    @NotNull
    @Contract(pure = true)
    static KonfigurationFactory kFactory() {
        return KonfigurationFactory.getInstanceV8();
    }


    /**
     * Get a boolean konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Boolean> bool(@NotNull String key);

    /**
     * Get a byte konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Byte> byte_(@NotNull String key);

    /**
     * Get a char konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Character> char_(@NotNull String key);

    /**
     * Get a short konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Short> short_(@NotNull String key);

    /**
     * Get an int konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Integer> int_(@NotNull String key);

    /**
     * Get a long konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Long> long_(@NotNull String key);

    /**
     * Get a float konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Float> float_(@NotNull String key);

    /**
     * Get a double konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Double> double_(@NotNull String key);

    /**
     * Get a string konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<String> string(@NotNull String key);


    /**
     * Get a list of U konfiguration value.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the list.
     * @param <U>  generic type of elements in the list.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
    <U> K<List<U>> list(@NotNull String key,
                        @NotNull Kind<U> type);

    /**
     * Get a set of konfiguration value.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the set.
     * @param <U>  generic type of elements in the set.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
    <U> K<Set<U>> set(@NotNull String key,
                      @NotNull Kind<U> type);

    /**
     * Get a custom object of type Q konfiguration value.
     *
     * <p><b>Important:</b> the underlying konfiguration source must support
     * this!
     *
     * <p><b>Important:</b> this method must <em>NOT</em> be used to obtain
     * maps, lists or sets. Use the corresponding methods
     * {@link #list(String, Kind)} and * {@link #set(String, Kind)}.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of the requested value.
     * @param <U>  generic type of requested value.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
    <U> K<U> custom(@NotNull String key,
                    @NotNull Kind<U> type);


    // =========================================================================

    /**
     * Check if {@code key} exists in the configuration.
     *
     * @param key  the config key to check it's existence
     * @param type type of konfiguration value.
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
    boolean has(@NotNull String key,
                @NotNull Kind<?> type);

    /**
     * Get a subset view of this konfiguration representing all the values under
     * the namespace of supplied key.
     *
     * @param key the key to which the scope of returned konfiguration is
     *            limited.
     * @return a konfiguration whose scope is limited to the supplied key.
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull
    @Contract(pure = true)
    Konfiguration subset(@NotNull String key);


    // =========================================================================

    /**
     * Register a listener to be notified of any updates to the konfiguration.
     * <p>
     * <em>DOES</em> hold an strong reference to the observer.
     * <p>
     * {@link #registerSoft(KeyObserver)} on the other hand, holds an string
     * reference to the observer until it is deregistered.
     *
     * @param observer the listener to register.
     * @return handle usable for deregister.
     * @see #registerSoft(KeyObserver)
     * @see #register(KeyObserver, String)
     */
    @NotNull
    @Contract(mutates = "this")
    default Handle register(@NotNull final KeyObserver observer) {
        return this.register(observer, KeyObserver.LISTEN_TO_ALL);
    }

    /**
     * Register a listener to be notified of updates to a key.
     *
     * <em>DOES</em> hold an strong reference to the observer.
     * <p>
     * {@link #registerSoft(KeyObserver, String)} on the other hand, does
     * <em>NOT</em> holds an strong reference to the observer until it is
     * deregistered.
     *
     * @param observer the listener to register.
     * @param key      the key to listen too.
     * @return handle usable for deregister().
     * @see #registerSoft(KeyObserver, String)
     */
    @NotNull
    @Contract(mutates = "this")
    Handle register(@NotNull KeyObserver observer,
                    @NotNull String key);


    /**
     * Register a listener to be notified of any updates to the konfigurations.
     * <p>
     * Does <em>NOT</em> hold an strong reference to the observer, uses weak
     * references.
     * <p>
     * {@link #register(KeyObserver)} on the other hand, holds an string
     * reference to the observer until it is deregistered.
     *
     * @param observer the listener to register.
     * @return handle usable for deregister.
     * @see #register(KeyObserver)
     */
    @NotNull
    @Contract(mutates = "this")
    default Handle registerSoft(@NotNull final KeyObserver observer) {
        return this.registerSoft(observer, KeyObserver.LISTEN_TO_ALL);
    }

    /**
     * Register a listener to be notified of updates to a key.
     * <p>
     * Does <em>NOT</em> hold an strong reference to the observer, uses weak
     * references.
     * <p>
     * {@link #register(KeyObserver, String)} on the other hand, holds an string
     * reference to the observer until it is deregistered.
     *
     * @param observer the listener to register.
     * @param key      the key to listen too.
     * @return handle usable for deregister().
     * @see #register(KeyObserver, String)
     */
    @NotNull
    @Contract(mutates = "this")
    Handle registerSoft(@NotNull KeyObserver observer,
                        @NotNull String key);


    /**
     * Deregister a previously registered listener of a key.
     *
     * @param key      the key to deregister from.
     * @param observer handle returned by one of register methods.
     */
    @Contract(mutates = "this")
    void deregister(@NotNull Handle observer,
                    @NotNull String key);

    /**
     * Deregister a previously registered listener of a key, from <em>ALL</em>
     * keys.
     *
     * @param observer handle returned by one of register methods.
     */
    @Contract(mutates = "this")
    default void deregister(@NotNull final Handle observer) {
        this.deregister(observer, KeyObserver.LISTEN_TO_ALL);
    }


    // =========================================================================

    /**
     * Name of this konfiguration. Helps with debugging and readability.
     *
     * @return Name of this configuration.
     */
    @NotNull
    @Contract(pure = true)
    String name();

    /**
     * Manager object associated with this konfiguration.
     *
     * @return manager associated with this konfiguration, or empty if this
     * method has been already called before.
     */
    @Contract(mutates = "this")
    Optional<KonfigurationManager> manager();

}
