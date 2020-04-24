package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.*;
import io.koosha.konfiguration.K;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.lang.String.format;

@ThreadSafe
@ApiStatus.Internal
final class Kombiner_K<U> implements K<U> {

    @NotNull
    private final Kombiner origin;

    @NotNull
    private final String key;

    @NotNull
    private final Kind<U> type;

    public Kombiner_K(@NotNull final Kombiner origin,
                      @NotNull final String key,
                      @NotNull final Kind<U> type) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        this.origin = origin;
        this.key = key;
        this.type = type;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String key() {
        return this.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Kind<U> type() {
        return this.type;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public U v() {
        return this.origin.values.v(key, this.type, null, true);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public U vn() {
        final U v = this.v();

        if (v == null)
            throw new KfgMissingKeyException(this.origin.name(), this.key, this.type);

        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public boolean exists() {
        return origin.has(this.key, this.type);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Handle registerSoft(@NotNull final KeyObserver keyObserver) {
        Objects.requireNonNull(keyObserver, "keyObserver");
        return origin.registerSoft(keyObserver, this.key);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @NotNull
    public Handle register(@NotNull final KeyObserver keyObserver) {
        Objects.requireNonNull(keyObserver, "keyObserver");
        return this.origin.register(keyObserver, this.key);
    }

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
     * @param observerHandle listener being registered for key {@link #key()}
     * @return this
     * @see #register(KeyObserver)
     */
    @Override
    @NotNull
    public K<U> deregister(@NotNull Handle observerHandle) {
        Objects.requireNonNull(observerHandle, "observerHandle");
        this.origin.deregister(observerHandle, this.key);
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        try {
            return format("K(%s=%s)", this.key, this.v());
        }
        catch (final Exception e) {
            return format("K(%s)::error", this.key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Kombiner_K))
            return false;
        final Kombiner_K<?> other = (Kombiner_K<?>) o;
        return Objects.equals(this.origin, other.origin)
                && Objects.equals(this.key, other.key)
                && Objects.equals(this.type, other.type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = PRIME + this.origin.hashCode();
        result = result * PRIME + this.key.hashCode();
        result = result * PRIME + this.type.hashCode();
        return result;
    }

}
