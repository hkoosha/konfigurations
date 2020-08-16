package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Konfig value wrapper.
 *
 * <p>All the methods denoted with 'Thread-safe' in their comment section must
 * be implemented in a thread safe fashion.
 *
 * @param <U> type of value being wrapped
 */
@SuppressWarnings("unused")
@ThreadSafe
@Immutable
public interface K<U> {

    boolean supportsRegister();

    /**
     * Register to receive update notifications for changes in value of this
     * konfiguration value, and this value only.
     *
     * <p>listeners may register to multiple keys on different instances of this
     * interface, but registering to the same key multiple times has no special
     * effect (it's only registered once).
     *
     * @param observer listener being registered for key {@link #key()}
     * @return this
     * @see #deregister(Handle)
     */
    @NotNull
    @Contract(mutates = "this")
    Handle registerSoft(@NotNull KeyObserver observer);

    /**
     * Register to receive update notifications for changes in value of this
     * konfiguration value, and this value only.
     *
     * <p>listeners may register to multiple keys on different instances of this
     * interface, but registering to the same key multiple times has no special
     * effect (it's only registered once).
     *
     * @param observer listener being registered for key {@link #key()}
     * @return this
     * @see #deregister(Handle)
     */
    @NotNull
    @Contract(mutates = "this")
    Handle register(@NotNull KeyObserver observer);

    /**
     * De-register a listener previously registered via
     * {@link #register(KeyObserver)}.
     *
     * <p>De-registering a previously de-registered listener, or a listener not
     * previously registered at all has no effect.
     *
     * <p>Thread-safe.
     *
     * <p><b>IMPORTANT:</b> Do NOT just pass in lambdas, as this method stores
     * only weak references and the observer will be garbage collected. Keep a
     * reference to the observer yourself.
     *
     * @param observer listener being registered for key {@link #key()}
     * @return this
     * @see #register(KeyObserver)
     */
    @NotNull
    @Contract(mutates = "this")
    K<U> deregister(@NotNull Handle observer);


    /**
     * Unique key of this konfiguration.
     *
     * <p>Thread-safe.
     *
     * @return unique key of this konfiguration.
     */
    @NotNull
    @Contract(pure = true)
    String key();

    /**
     * Underlying type represented by this konfig key.
     *
     * @return Underlying type represented by this konfig key.
     */
    @Nullable
    @Contract(pure = true)
    Kind<U> type();

    /**
     * If the value denoted by {@link #key()} in the original source exists.
     *
     * <p>Thread-safe.
     *
     * @return If the value denoted by {@link #key()} in the original source
     * exists.
     */
    @Contract(pure = true)
    boolean exists();

    /**
     * If the value denoted by {@link #key()} in the original source exists and
     * it's value is not null.
     *
     * <p>Thread-safe.
     *
     * @return If the value denoted by {@link #key()} in the original source
     * exists and is not null.
     */
    @Contract(pure = true)
    default boolean existsNonNull() {
        return this.exists() && this.v() != null;
    }


    /**
     * Actual value of this konfiguration.
     *
     * <p>Thread-safe.
     *
     * @return Actual value of this konfiguration.
     * @throws KfgMissingKeyException if the value has been removed from
     *                                original konfiguration source.
     * @see #v(Object)
     */
    @Nullable
    U v();

    /**
     * Same as {@link #v()} but in case of null throws {@link KfgMissingKeyException}.
     *
     * @return value this konfig holds, but throws {@link KfgMissingKeyException} if that value is going to be null.
     * @see #v(Object)
     */
    @NotNull
    U vn();

    /**
     * Similar to {@link #v()}, but returns the supplied default if this
     * konfiguration's key no longer exists in the source.
     *
     * <p>Thread-safe.
     *
     * @param defaultValue default value to use if key of this konfiguration
     *                     has been removed from the original source.
     * @return actual value of this konfiguration, or defaultValue if the key
     * of this konfiguration has been removed from the original source.
     * @see #v()
     */
    @Nullable
    default U v(@Nullable final U defaultValue) {
        // this.exits() is not atomic.
        try {
            return this.v();
        }
        catch (final KfgMissingKeyException mk) {
            return defaultValue;
        }
    }

    @Nullable
    default U vn(@NotNull final U defaultValue) {
        Objects.requireNonNull(defaultValue,
            "defaultValue for vn() can not be null, you may use the v() variant instead");

        // this.exits() is not atomic.
        try {
            final U v = this.v();
            return v == null ? defaultValue : v;
        }
        catch (final KfgMissingKeyException mk) {
            return defaultValue;
        }
    }

}
