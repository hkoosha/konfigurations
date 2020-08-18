package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @Nullable
    Boolean bool(@NotNull String key);

    @Nullable
    Boolean bool(@NotNull String key,
                 @Nullable Boolean def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration put(@NotNull String key,
                          @Nullable Boolean value);


    /**
     * Get a byte konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @Nullable
    Byte byte_(@NotNull String key);

    @Nullable
    Byte byte_(@NotNull String key,
               @Nullable Byte def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration put(@NotNull String key,
                          @Nullable Byte value);


    /**
     * Get a char konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @Nullable
    Character char_(@NotNull String key);

    @Nullable
    Character char_(@NotNull String key,
                    @Nullable Character def);


    /**
     * Get a short konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @Nullable
    Short short_(@NotNull String key);

    @Nullable
    Short short_(@NotNull String key,
                 @Nullable Short def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration put(@NotNull String key,
                          @Nullable Short value);


    /**
     * Get an int konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @Nullable
    Integer int_(@NotNull String key);

    @Nullable
    Integer int_(@NotNull String key,
                 @Nullable Integer def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration put(@NotNull String key,
                          @Nullable Integer value);


    /**
     * Get a long konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @Nullable
    Long long_(@NotNull String key);

    @Nullable
    Long long_(@NotNull String key,
               @Nullable Long def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration put(@NotNull String key,
                          @Nullable Long value);


    /**
     * Get a float konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @Nullable
    Float float_(@NotNull String key);

    @Nullable
    Float float_(@NotNull String key,
                 @Nullable Float def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration put(@NotNull String key,
                          @Nullable Float value);


    /**
     * Get a double konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @Nullable
    Double double_(@NotNull String key);

    @Nullable
    Double double_(@NotNull String key,
                   @Nullable Double def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration put(@NotNull String key,
                          @Nullable Double value);


    /**
     * Get a string konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @Nullable
    String string(@NotNull String key);

    @Nullable
    String string(@NotNull String key,
                  @Nullable String def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration put(@NotNull String key,
                          @Nullable String value);


    /**
     * Get a list of U konfiguration value.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the list.
     * @param <U>  generic type of elements in the list.
     * @return konfiguration value wrapper for the requested key.
     */
    @Nullable <U> List<U> list(@NotNull String key,
                               @NotNull Kind<U> type);

    @Nullable <U> List<U> list(@NotNull String key,
                               @NotNull Kind<U> type,
                               @Nullable List<U> def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration put(@NotNull String key,
                          @Nullable List<?> value);


    /**
     * Get a set of konfiguration value.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the set.
     * @param <U>  generic type of elements in the set.
     * @return konfiguration value wrapper for the requested key.
     */
    @Nullable <U> Set<U> set(@NotNull String key,
                             @NotNull Kind<U> type);

    @Nullable <U> Set<U> set(@NotNull String key,
                             @NotNull Kind<U> type,
                             @Nullable Set<U> def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration put(@NotNull String key,
                          @Nullable Set<?> value);


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
    @Nullable <U> U custom(@NotNull String key,
                           @NotNull Kind<U> type);

    @Nullable <U> U custom(@NotNull String key,
                           @NotNull Kind<U> type,
                           @Nullable U def);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration putCustom(@NotNull String key,
                                @Nullable Object value);

    @NotNull
    @Contract(mutates = "this")
    LiteKonfiguration delete(@NotNull final String key);

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

    @NotNull
    @Contract(pure = true)
    String serialize();

    @Contract(pure = true)
    boolean isReadonly();

    @NotNull
    @Contract(pure = true)
    LiteKonfiguration toReadonly();

    @NotNull
    @Contract(pure = true)
    LiteKonfiguration toWritableCopy();

}
