package io.koosha.konfiguration.ext;

import io.koosha.konfiguration.KfgAssertionException;
import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.Nullable;

class KfgSnakeYamlAssertionError extends KfgAssertionException {

    public KfgSnakeYamlAssertionError(@Nullable final String source,
                                      @Nullable final String message) {
        super(source, null, null, null, message);
    }

    public KfgSnakeYamlAssertionError(@Nullable final String source,
                                      @Nullable final String key,
                                      @Nullable final Kind<?> neededType,
                                      @Nullable final Object actualValue,
                                      @Nullable final String message) {
        super(source, key, neededType, actualValue, message);
    }

}
