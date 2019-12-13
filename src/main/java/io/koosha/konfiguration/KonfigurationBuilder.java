package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collection;

@ThreadSafe
@ApiStatus.AvailableSince(Factory.VERSION_8)
public interface KonfigurationBuilder {

    @NotNull
    KonfigurationBuilder add(@NotNull Konfiguration konfig);

    @NotNull
    KonfigurationBuilder add(@NotNull Konfiguration konfig, @NotNull Konfiguration... k);

    @NotNull
    KonfigurationBuilder add(@NotNull Collection<Konfiguration> konfig);

    @NotNull
    KonfigurationBuilder fairLock(boolean fair);

    @NotNull
    KonfigurationBuilder lockWaitTime(
            @Range(from = 0,
                    to = Long.MAX_VALUE) long waitTime);

    @NotNull
    KonfigurationBuilder lockNoWait();

    @NotNull
    Konfiguration build();

}
