package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.Nullable;

/**
 * This shouldn't happen. This must be prevented by konfigurations.
 */
public class KfgAssertionException extends KfgException {

    public KfgAssertionException(@Nullable final String source,
                                 @Nullable final String key,
                                 @Nullable final Kind<?> neededType,
                                 @Nullable final Object actualValue,
                                 @Nullable final String message) {
        super(source, key, neededType, actualValue, message);
    }

}
