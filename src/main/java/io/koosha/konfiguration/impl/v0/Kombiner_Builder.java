package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationBuilder;
import lombok.NonNull;
import lombok.Synchronized;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

@ThreadSafe
@ApiStatus.Internal
final class Kombiner_Builder implements KonfigurationBuilder {

    @NotNull
    private final String name;

    @Nullable
    private List<Konfiguration> sources;

    @Nullable
    private Long lockWaitTime;

    private boolean fair = true;

    Kombiner_Builder(@NotNull @NonNull final String name) {
        this.name = name;
        this.sources = new ArrayList<>();
    }

    @Override
    @NotNull
    @Synchronized
    public KonfigurationBuilder add(@NotNull @NonNull final Konfiguration konfig) {
        this.ensure().add(konfig);
        return this;
    }

    @Override
    @NotNull
    @Synchronized
    public KonfigurationBuilder add(@NotNull Konfiguration konfig,
                                    @NotNull @NonNull Konfiguration... k) {
        this.ensure().add(konfig);
        this.ensure().addAll(asList(k));
        return this;
    }

    @Override
    @NotNull
    @Synchronized
    public KonfigurationBuilder add(@NotNull @NonNull final Collection<Konfiguration> konfig) {
        this.ensure().addAll(konfig);
        return this;
    }

    @Override
    @NotNull
    @Synchronized
    public KonfigurationBuilder fairLock(final boolean fair) {
        //noinspection ResultOfMethodCallIgnored
        this.ensure();
        this.fair = fair;
        return this;
    }

    @Override
    @Synchronized
    @NotNull
    public KonfigurationBuilder lockWaitTime(final long waitTime) {
        //noinspection ResultOfMethodCallIgnored
        this.ensure();
        this.lockWaitTime = waitTime;
        return this;
    }

    @Override
    @NotNull
    @Synchronized
    public KonfigurationBuilder lockNoWait() {
        //noinspection ResultOfMethodCallIgnored
        this.ensure();
        this.lockWaitTime = null;
        return this;
    }

    @Override
    @Synchronized
    @NotNull
    public Konfiguration build() {
        final List<Konfiguration> s = this.ensure();
        this.sources = null;
        return new Kombiner(this.name, s, this.lockWaitTime, this.fair);
    }


    @Synchronized
    private List<Konfiguration> ensure() {
        if (this.sources == null)
            throw new IllegalStateException("this builder is already consumed.");
        return this.sources;
    }

}
