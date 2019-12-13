package io.koosha.konfiguration.ext;

import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.Q;
import org.jetbrains.annotations.Nullable;

public final class KfgJacksonError extends KfgSourceException {

    public KfgJacksonError(@Nullable String source,
                           @Nullable String message) {
        super(source, message);
    }

    public KfgJacksonError(@Nullable String source,
                           @Nullable String message,
                           @Nullable Throwable cause) {
        super(source, message, cause);
    }

    public KfgJacksonError(@Nullable String source,
                           @Nullable String key,
                           @Nullable Q<?> neededType,
                           @Nullable Object actualValue,
                           @Nullable String message,
                           @Nullable Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
    }

    public KfgJacksonError(@Nullable String source,
                           @Nullable String key,
                           @Nullable Q<?> neededType,
                           @Nullable Object actualValue,
                           @Nullable String message) {
        super(source, key, neededType, actualValue, message);
    }

    public KfgJacksonError(@Nullable String source,
                           @Nullable String key,
                           @Nullable Q<?> neededType,
                           @Nullable Object actualValue,
                           @Nullable Throwable cause) {
        super(source, key, neededType, actualValue, cause);
    }

    public KfgJacksonError(@Nullable String source,
                           @Nullable String key,
                           @Nullable Q<?> neededType,
                           @Nullable Object actualValue) {
        super(source, key, neededType, actualValue);
    }

}
