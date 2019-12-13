package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.Konfiguration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
@ApiStatus.NonExtendable
interface Konfiguration0 extends Konfiguration {

    /**
     * Manager object associated with this konfiguration.
     *
     * @return On first invocation, a manager instance. An second invocation
     * and on, throws exception.
     * @throws KfgIllegalStateException if manager is already called once
     *                                  before.
     */
    @NotNull
    @Override
    KonfigurationManager0 manager();

}
