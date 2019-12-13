package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@ThreadSafe
@ApiStatus.Internal
final class Kombiner_K<U> implements K<U> {

    @NonNull
    @NotNull
    private final Kombiner origin;

    @NonNull
    @NotNull
    @Getter
    private final String key;

    @NonNull
    @NotNull
    @Getter
    private final Q<U> type;


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
    public Handle registerSoft(@NonNull @NotNull KeyObserver observer) {
        return origin.registerSoft(observer, this.key);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @NotNull
    public Handle register(@NonNull @NotNull final KeyObserver observer) {
        return this.origin.register(observer, this.key);
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
     * @param observer listener being registered for key {@link #key()}
     * @return this
     * @see #register(KeyObserver)
     */
    @Override
    @NotNull
    public K<U> deregister(@NonNull @NotNull Handle observer) {
        this.origin.deregister(observer, this.key);
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

}
