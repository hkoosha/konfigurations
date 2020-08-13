package io.koosha.konfiguration.lite;

import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * All methods are thread-safe (and should be implemented as such).
 */
@SuppressWarnings("unused")
@ThreadSafe
public interface LiteKonfiguration {

    /**
     * Get a boolean konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    Boolean bool(@NotNull String key);

    Boolean bool(@NotNull String key,
                 Boolean def);

    LiteKonfiguration put(@NotNull String key,
                          Boolean value);


    /**
     * Get a byte konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    Byte byte_(@NotNull String key);

    Byte byte_(@NotNull String key,
               Byte def);

    LiteKonfiguration put(@NotNull String key,
                          Byte value);


    /**
     * Get a char konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    Character char_(@NotNull String key);

    Character char_(@NotNull String key,
                    Character def);

    LiteKonfiguration put(@NotNull String key,
                          Character value);


    /**
     * Get a short konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    Short short_(@NotNull String key);

    Short short_(@NotNull String key,
                 Short def);

    LiteKonfiguration put(@NotNull String key,
                          Short value);


    /**
     * Get an int konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    Integer int_(@NotNull String key);

    Integer int_(@NotNull String key,
                 Integer def);

    LiteKonfiguration put(@NotNull String key,
                          Integer value);


    /**
     * Get a long konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    Long long_(@NotNull String key);

    Long long_(@NotNull String key,
               Long def);

    LiteKonfiguration put(@NotNull String key,
                          Long value);


    /**
     * Get a float konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    Float float_(@NotNull String key);

    Float float_(@NotNull String key,
                 Float def);

    LiteKonfiguration put(@NotNull String key,
                          Float value);


    /**
     * Get a double konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    Double double_(@NotNull String key);

    Double double_(@NotNull String key,
                   Double def);

    LiteKonfiguration put(@NotNull String key,
                          Double value);


    /**
     * Get a string konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    String string(@NotNull String key);

    String string(@NotNull final String key,
                  final String def);

    LiteKonfiguration put(@NotNull String key,
                          String value);


    /**
     * Get a list of U konfiguration value.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the list.
     * @param <U>  generic type of elements in the list.
     * @return konfiguration value wrapper for the requested key.
     */
    <U> List<U> list(@NotNull String key,
                     @NotNull Kind<U> type);

    <U> List<U> list(@NotNull String key,
                     @NotNull Kind<U> type,
                     List<U> def);

    LiteKonfiguration put(@NotNull String key,
                          List<?> value);


    /**
     * Get a set of konfiguration value.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the set.
     * @param <U>  generic type of elements in the set.
     * @return konfiguration value wrapper for the requested key.
     */
    <U> Set<U> set(@NotNull String key,
                   @NotNull Kind<U> type);

    <U> Set<U> set(@NotNull String key,
                   @NotNull Kind<U> type,
                   Set<U> def);

    LiteKonfiguration put(@NotNull String key,
                          Set<?> value);


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
    <U> U custom(@NotNull String key,
                 @NotNull Kind<U> type);

    <U> U custom(@NotNull String key,
                 @NotNull Kind<U> type,
                 U def);

    LiteKonfiguration putCustom(@NotNull String key,
                                Object value);

    // =========================================================================

    /**
     * Check if {@code key} exists in the configuration.
     *
     * @param key  the config key to check it's existence
     * @param type type of konfiguration value.
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
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
    LiteKonfiguration subset(@NotNull String key);


    // =========================================================================

    /**
     * Name of this konfiguration. Helps with debugging and readability.
     *
     * @return Name of this configuration.
     */
    @NotNull
    @Contract(pure = true)
    String name();

    String serialize();

}
