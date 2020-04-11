package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
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
    K<Character> char_(String key);

    /**
     * Get a short konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Short> short_(String key);

    /**
     * Get an int konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Integer> int_(String key);

    /**
     * Get a long konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Long> long_(String key);

    /**
     * Get a float konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Float> float_(String key);

    /**
     * Get a double konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<Double> double_(String key);

    /**
     * Get a string konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    K<String> string(String key);


    /**
     * Get a list of konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    default K<List<?>> list(@NotNull final String key) {
        return (K) list(key, (Q) Q.UNKNOWN_LIST);
    }

    /**
     * Get a map of U to V konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})

    @NotNull
    default K<Map<?, ?>> map(@NotNull final String key) {
        return (K) map(key, (Q) Q.UNKNOWN_MAP);
    }

    /**
     * Get a set of konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})

    @NotNull
    default K<Set<?>> set(@NotNull final String key) {
        return (K) set(key, (Q) Q.UNKNOWN_SET);
    }


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
    <U> K<List<U>> list(String key, @Nullable Q<List<U>> type);

    /**
     * Get a map of U to V konfiguration value.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type generic type of map
     * @param <U>  generic type of map, the key type.
     * @param <V>  generic type of map, the value type.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
    <U, V> K<Map<U, V>> map(@NotNull String key, @Nullable Q<Map<U, V>> type);

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
    <U> K<Set<U>> set(@NotNull String key, @Nullable Q<Set<U>> type);


    /**
     * Get a custom object, type depends on underlying sources.
     *
     * <p><b>Important:</b> the underlying konfiguration source must support
     * this!
     *
     * <p><b>Important:</b> this method must <em>NOT</em> be used to obtain
     * maps, lists or sets. Use the corresponding methods
     * {@link #map(String, Q)}, {@link #list(String, Q)} and
     * {@link #set(String, Q)}.
     *
     * @param key unique key of the konfiguration being requested.
     * @param <U> generic type of the returned konfig value.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
    default <U> K<U> custom(@NotNull final String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a custom object of type Q konfiguration value.
     *
     * <p><b>Important:</b> the underlying konfiguration source must support
     * this!
     *
     * <p><b>Important:</b> this method must <em>NOT</em> be used to obtain
     * maps, lists or sets. Use the corresponding methods
     * {@link #map(String, Q)}, {@link #list(String, Q)} and
     * {@link #set(String, Q)}.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of the requested value.
     * @param <U>  generic type of requested value.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
    <U> K<U> custom(String key, @Nullable Q<U> type);


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
    boolean has(@NotNull String key, @Nullable Q<?> type);

    /**
     * Get a subset view of this konfiguration representing all the values under
     * the namespace of supplied key.
     *
     * @param key the key to which the scope of returned konfiguration is
     *            limited.
     * @return a konfiguration whose scope is limited to the supplied key.
     */
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
     * @return this.
     */
    @NotNull
    @Contract(mutates = "this")
    Konfiguration deregister(@NotNull Handle observer,
                             @NotNull String key);

    /**
     * Deregister a previously registered listener of a key, from <em>ALL</em>
     * keys.
     *
     * @param observer handle returned by one of register methods.
     * @return this.
     */
    @NotNull
    @Contract(mutates = "this")
    default Konfiguration deregister(@NotNull Handle observer) {
        return this.deregister(observer, KeyObserver.LISTEN_TO_ALL);
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
     * <p>
     * Returns null if {@link #getManagerAndSetItToNull()} has been called
     * previously.
     *
     * @return manager associated with this konfiguration or null if
     * {@link #getManagerAndSetItToNull()} has been called previously.
     * @see #getManagerAndSetItToNull()
     */
    @Contract(pure = true)
    KonfigurationManager manager();

    /**
     * Manager object associated with this konfiguration.
     * <p>
     * Returns null if this method has been called previously.
     *
     * @return manager associated with this konfiguration or null if this
     * method has been called previously.
     * @see #getManagerAndSetItToNull()
     */
    @Nullable
    @Contract(mutates = "this")
    KonfigurationManager getManagerAndSetItToNull();

}
