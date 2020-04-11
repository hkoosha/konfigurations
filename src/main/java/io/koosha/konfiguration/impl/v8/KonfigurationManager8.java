package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgAssertionException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyMap;

@ApiStatus.Internal
@ApiStatus.NonExtendable
interface KonfigurationManager8 extends KonfigurationManager {

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @NotNull
    @Override
    default Map<String, Collection<Runnable>> update() {
        return emptyMap();
    }

    /**
     * {@inheritDoc}
     */
    @Contract("-> fail")
    @Override
    default Konfiguration getAndSetToNull() {
        throw new KfgAssertionException("shouldn't be called");
    }

    /**
     * {@inheritDoc}
     */
    @Contract("-> fail")
    @Override
    default void updateNow() {
        throw new KfgAssertionException("shouldn't be called");
    }

    @NotNull
    @ApiStatus.Internal
    Source _update();

}
