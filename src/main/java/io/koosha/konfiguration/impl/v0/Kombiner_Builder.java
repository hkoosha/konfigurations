package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationBuilder;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

@ThreadSafe
@ApiStatus.Internal
final class Kombiner_Builder implements KonfigurationBuilder {

    private final Object LOCK = new Object();

    @NotNull
    private final String name;

    @Nullable
    private List<Konfiguration> sources;

    @Nullable
    private Long lockWaitTime;

    private boolean fair = true;

    Kombiner_Builder(@NotNull final String name) {
        Objects.requireNonNull(name, "name");
        this.name = name;
        this.sources = new ArrayList<>();
    }

    @Override
    @NotNull
    public KonfigurationBuilder add(@NotNull final Konfiguration konfig) {
        Objects.requireNonNull(konfig, "konfig");
        synchronized (LOCK) {
            this.ensure().add(konfig);
        }
        return this;
    }

    @Override
    @NotNull
    public KonfigurationBuilder add(@NotNull final Konfiguration konfig,
                                    @NotNull final Konfiguration... k) {
        Objects.requireNonNull(k, "k");
        synchronized (LOCK) {
            this.ensure().add(konfig);
            this.ensure().addAll(asList(k));
        }
        return this;
    }

    @Override
    @NotNull
    public KonfigurationBuilder add(@NotNull final Collection<Konfiguration> konfig) {
        Objects.requireNonNull(konfig, "konfig");
        synchronized (LOCK) {
            this.ensure().addAll(konfig);
        }
        return this;
    }

    @Override
    @NotNull
    public KonfigurationBuilder fairLock(final boolean fair) {
        synchronized (LOCK) {
            //noinspection ResultOfMethodCallIgnored
            this.ensure();
            this.fair = fair;
        }
        return this;
    }

    @Override
    @NotNull
    public KonfigurationBuilder lockWaitTime(final long waitTime) {
        synchronized (LOCK) {
            //noinspection ResultOfMethodCallIgnored
            this.ensure();
            this.lockWaitTime = waitTime;
        }
        return this;
    }

    @Override
    @NotNull
    public KonfigurationBuilder lockNoWait() {
        synchronized (LOCK) {
            //noinspection ResultOfMethodCallIgnored
            this.ensure();
            this.lockWaitTime = null;
        }
        return this;
    }

    @Override
    @NotNull
    public Konfiguration build() {
        synchronized (LOCK) {
            final List<Konfiguration> s = this.ensure();
            this.sources = null;
            return new Kombiner(this.name, s, this.lockWaitTime, this.fair);
        }
    }


    private List<Konfiguration> ensure() {
        synchronized (LOCK) {
            if (this.sources == null)
                throw new IllegalStateException("this builder is already consumed.");
            return this.sources;
        }
    }

}
