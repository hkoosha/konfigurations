package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.KfgAssertionException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyMap;

@ApiStatus.Internal
@ApiStatus.NonExtendable
interface KonfigurationManager0 extends KonfigurationManager {

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Contract("-> fail")
    @NotNull
    @Override
    default Map<String, Collection<Runnable>> update() {
        return emptyMap();
    }

    /**
     * {@inheritDoc}
     */
    @Contract("-> fail")
    @Nullable
    @Override
    default Konfiguration getAndSetToNull() {
        throw new KfgAssertionException("shouldn't be called");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void updateNow() {
        throw new KfgAssertionException("shouldn't be called");
    }

    @NotNull
    @ApiStatus.Internal
    Konfiguration0 _update();

}
