package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgException;
import io.koosha.konfiguration.Q;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

/**
 * This shouldn't happen. This must be prevented by konfigurations.
 */
@ThreadSafe
class KfgAssertionException extends KfgException {

    public KfgAssertionException(@Nullable String source,
                                 @Nullable String key,
                                 @Nullable Q<?> neededType,
                                 @Nullable Object actualValue,
                                 @Nullable String message) {
        super(source, key, neededType, actualValue, message);
    }

}
