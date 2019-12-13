package io.koosha.konfiguration.ext;

import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.Q;
import org.jetbrains.annotations.Nullable;

public class KfgPreferencesError extends KfgSourceException {

    public KfgPreferencesError(@Nullable String source,
                               @Nullable String message) {
        super(source, message);
    }

    public KfgPreferencesError(@Nullable String source,
                               @Nullable String message,
                               @Nullable Throwable cause) {
        super(source, message, cause);
    }

    public KfgPreferencesError(@Nullable String source,
                               @Nullable String key,
                               @Nullable Q<?> neededType,
                               @Nullable Object actualValue,
                               @Nullable String message,
                               @Nullable Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
    }

    public KfgPreferencesError(@Nullable String source,
                               @Nullable String key,
                               @Nullable Q<?> neededType,
                               @Nullable Object actualValue,
                               @Nullable String message) {
        super(source, key, neededType, actualValue, message);
    }

    public KfgPreferencesError(@Nullable String source,
                               @Nullable String key,
                               @Nullable Q<?> neededType,
                               @Nullable Object actualValue,
                               @Nullable Throwable cause) {
        super(source, key, neededType, actualValue, cause);
    }

    public KfgPreferencesError(@Nullable String source,
                               @Nullable String key,
                               @Nullable Q<?> neededType, @Nullable Object actualValue) {
        super(source, key, neededType, actualValue);
    }

}
