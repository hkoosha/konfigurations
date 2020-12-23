package io.koosha.konfiguration.impl;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ThreadSafe
@ApiStatus.Internal
final class KombinerK<U> implements K<U> {

    @NotNull
    private final Kombiner origin;

    @NotNull
    private final String key;

    @NotNull
    private final Kind<U> type;

    public KombinerK(@NotNull final Kombiner origin,
                     @NotNull final String key,
                     @NotNull final Kind<U> type) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        this.origin = origin;
        this.key = key;
        this.type = type;
    }


    @Override
    @NotNull
    public String key() {
        return this.key;
    }

    @Override
    @NotNull
    public Kind<U> type() {
        return this.type;
    }


    @Override
    @Nullable
    public U v() {
        return this.origin.getCachedValueOrIssueIt(key, this.type);
    }

    @NotNull
    @Override
    public U vn() {
        final U v = this.v();

        if (v == null)
            throw new KfgMissingKeyException(this.origin.name(), this.key, this.type);

        return v;
    }

    @Override
    @Contract(pure = true)
    public boolean exists() {
        return this.origin.has(this.key, this.type);
    }

    @Override
    @NotNull
    public Handle registerSoft(@NotNull final KeyObserver keyObserver) {
        Objects.requireNonNull(keyObserver, "keyObserver");
        return origin.registerSoft(keyObserver, this.key);
    }

    @Override
    @NotNull
    public Handle register(@NotNull final KeyObserver keyObserver) {
        Objects.requireNonNull(keyObserver, "keyObserver");
        return this.origin.register(keyObserver, this.key);
    }

    @Override
    @NotNull
    public K<U> deregister(@NotNull Handle observerHandle) {
        Objects.requireNonNull(observerHandle, "observerHandle");
        this.origin.deregister(observerHandle, this.key);
        return this;
    }

    @Override
    public boolean supportsRegister() {
        return this.origin.updatable;
    }



    @Override
    public String toString() {
        try {
            return String.format("K(%s=%s)", this.key, this.v());
        }
        catch (final Exception e) {
            return String.format("K(error::%s)", this.key);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof KombinerK))
            return false;
        final KombinerK<?> other = (KombinerK<?>) o;
        return Objects.equals(this.origin, other.origin)
            && Objects.equals(this.key, other.key)
            && Objects.equals(this.type, other.type);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = PRIME + this.origin.hashCode();
        result = result * PRIME + this.key.hashCode();
        result = result * PRIME + this.type.hashCode();
        return result;
    }

}
